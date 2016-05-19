class plantcomplex implements bioProcessBlock{
	double stemC;// gC/m2
    double rootC; // gC/m2
    double plantC;
    
    double cover;
    double barea_1;
	double barea;
    double vol;
    double vol_1;
    double depth;
    
    //algal
    double algalC;// gC/m3
    double algalN;// gN/m3
    double ks;
    double gs;
    double gi;
    double deadAlgalC;
    double deadAlgalN;
    
    ////
	double flux;
	double conc;
	double remove;//result
	double plantUptake;//result
	double algalUptake;//result
    
    // plant
    double currentTotalRootC;
    double currentTotalStemC;
    double deltaPlant; //<<----------- biomass
    double deltaPlantC; //<<----------- working with plantC
    double potPlantUptakeN;
    double potDentrification;
    double potAlgalUptakeN;
    double totalpot;
    
    
    
	public plantcomplex(double stem, double root, double cover_, double algae, double barea_, double depth_){
		
        
        cover = cover_;
        barea = barea_;
        barea_1 = 1.0/barea_;
        vol = barea*depth_;
        vol_1 = 1.0/vol;
        depth = depth_;
        
        stemC = stem;//gC/m2
        rootC = root;//gC/m2
        plantC = (stem + root)*cover;//<<------------------- holding
        
        
        //// algae
        algalC = algae; //mgC/m3
        algalN = algae*ref.algalNC; //mgN/m3
        deadAlgalC=0.0;
        deadAlgalN=0.0;
        
        //from chapter
        //ks = 0.01; //(guess)<===== m3/mgC
        
	}
	
    
    
    
    
    
    
    
	
	public double run(int waterp, int p, int time, int d, int h){
		
        if(ref.shortcut){p=1000; waterp=1000;}
        if(p<=0){algalUptake=0; plantUptake=0; remove=0; return 0.0;}
		flux = p*ref.parcel2flux;
        conc = flux;
        conc /= (waterp*ref.parcel2Q);//mg/m3
        
        //plant
        if(rootC+stemC>0){
            currentTotalRootC = rootC*cover*barea;
            currentTotalStemC = stemC*cover*barea;
            
            plantC = (stemC+rootC)*cover;
            deltaPlantC = ref.GMAX*plantC;// Math.exp(ref.GMAX)*plantC - plantC;
            plantC += deltaPlantC;  //<<----------------- plantC = biomass*cover
            
            cover = 0.1831/(1+Math.exp(-0.3*(time*0.0006944444-25) )); //convert day to min
            cover += 0.2215;
            cover += 0.1/(1+Math.exp(-0.1*(time*0.0006944444-100) ));
            
            deltaPlant = plantC/cover; // not correct here
            deltaPlant -= rootC;
            deltaPlant -= stemC;//
            deltaPlant /= stemC*ref.root_stemFr + stemC; //<<--- making this to stemc growth rate
            
            potPlantUptakeN =
            ((deltaPlant*stemC*ref.root_stemFr+rootC)*cover*barea -currentTotalRootC)*ref.rootNC +
            ((deltaPlant*stemC+stemC)*cover*barea -currentTotalStemC)*ref.stemNC;
            potPlantUptakeN *= 1000;
            
            
        }else{
            potPlantUptakeN=0.0;
            //potDentrification=0.0;
        }
        
        //denitrification
        potDentrification = barea*conc*0.6*Math.pow(10,-0.493*Math.log10(conc)-2.975);//Mulholland et al. - 2008(a); Vf = cm/s, [N]=ugN/L; 0.01 to m; s -> min
        
        //algae
        if( algalC >0){
            algalC *= ref.algalReduce;
            algalN *= ref.algalReduce;
            
            //self-regular (Newbold) chapter -- benthic algae!!!
            gs=1/(1+ref.algalKS*algalC); //<===== m3/mgC
            //Droop model 1984
            gi=(1-ref.algalqNC*algalC/algalN);// high gi if Qnc is small and bioN is big; (this factor is not limiting as CN goes down)
            
            potAlgalUptakeN = ref.algalUmax;
            potAlgalUptakeN *= conc;
            potAlgalUptakeN *= gs;
            potAlgalUptakeN *= algalC;
            potAlgalUptakeN /= (ref.algalKH+conc);
            potAlgalUptakeN *= vol;
        }else{
            potAlgalUptakeN=0.0;
        }
  
        
        totalpot = potPlantUptakeN + potDentrification + potAlgalUptakeN;
        if(totalpot > flux){
            //System.out.println("N limit");
            remove = potDentrification/totalpot*flux;
            
            if(stemC+rootC >0){
                //remove = potDentrification/totalpot*flux;
                
                plantUptake = potPlantUptakeN/totalpot*flux;
                deltaPlant = (plantUptake*0.001 +currentTotalRootC*ref.rootNC +currentTotalStemC*ref.stemNC -rootC*cover*barea*ref.rootNC -stemC*cover*barea*ref.stemNC) / (stemC*ref.root_stemFr*cover*barea*ref.rootNC +stemC*cover*barea*ref.stemNC);
                //deltaPlant /= (stemC*ref.root_stemFr*ref.rootNC + stemC*ref.stemNC);
                rootC += deltaPlant*ref.root_stemFr*stemC;  //<<----
                stemC += deltaPlant*stemC; //<<----//
            }else{
                plantUptake=0.0;
                //remove=0.0;
            }
            
            if( algalC>0){
                algalUptake = potAlgalUptakeN/totalpot*flux;
                algalN += algalUptake*vol_1;
                algalC += algalUptake*vol_1*ref.algalqNC;
            }else{
                algalUptake=0.0;
            }

            //System.out.printf("%d,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",time,cover,stemC,rootC,plantC,deltaPlant,flux,algalUptake,plantUptake,remove);
            
        }else{
            remove = potDentrification;
            
            if(stemC+rootC >0){
                //remove = potDentrification;
                
                rootC += deltaPlant*ref.root_stemFr*stemC;  //<<----
                stemC += deltaPlant*stemC; //<<----
                plantUptake = potPlantUptakeN;
            }else{
                //remove=0.0;
                plantUptake=0.0;
            }
            
            if( algalC>0){
                algalC += gs*gi*ref.algalGMAX*algalC;
                algalN += potAlgalUptakeN*vol_1;
                algalUptake = potAlgalUptakeN;
            }else{
                algalUptake=0.0;
            }
            
            //System.out.printf("cover=%f,stemc=%f,rootc=%f,deltaPlant=%f,deltaPlantC=%f\n",cover,stemC,rootC,deltaPlant,deltaPlantC);
            //System.out.printf("%d,%f,%f,%f,%f,%f,%f,%f,%f,%f\n",time,cover,stemC,rootC,plantC,deltaPlant,flux,algalUptake,plantUptake,remove);
            //System.out.printf("gs=%f,gi=%f,algalGMAX=%f,algalC=%f,algalCN=%f,%f,%f\n",gs,gi,ref.algalGMAX,algalC,algalC/algalN, ref.algalUmax*conc/(ref.algalKH+conc), conc);
        }
        
		return (algalUptake+plantUptake+remove);
	}//run
	
	public double getPlantC(){return stemC+rootC;} //gC/m2
    public double getAlgalC(){return algalC;} //mgC/m3
    public double getAlgalN(){return algalN;}
    public double getDeadAlgalC(){return deadAlgalC;}
    public double getDeadAlgalN(){return deadAlgalN;}
    public void add2AlgalC(double cc){algalC+=cc;}
    public void add2AlgalN(double nn){algalN+=nn;}
    
    
    public double getPlantUptake(){return plantUptake;}
    public double getAlgalUptake(){return algalUptake;}
    public double getRemove(){return remove;}
    
    
}