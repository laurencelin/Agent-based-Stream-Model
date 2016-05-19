public class streamZone {
	
    String name;
    double depth; //average
    
	parcelList income;
    double incomeAlgaeC;
    double incomeAlgaeN;
	parcelList hold;
	streamZone next;
	//gamma distribution
	int ttmaxMean;
	int g0;
	
    int numOutN;
    int numOutQ;
    int index;
    int chosen;
    double tmp;
    double step;
    double randt;
    int randtt;
    double randtmp;
    
    int[] randtable; //1000 random exp
    double[] randlist; //1000 random unif
    
	bioProcessBlock bio;
	double remove; //actual flux
	int remove_parcel; // (in parcel unit)
	double remove_bal; // (in parcel unit)
	
    //-----------------------------------------------------------
    
    
    
	public streamZone(String name_, int ttmaxMean_, bioProcessBlock bio_){
        name = name_;
        
		income = new parcelList();
        incomeAlgaeC=0.0;
        incomeAlgaeN=0.0;
		hold = new parcelList();
		next = null;
		ttmaxMean = ttmaxMean_;
		bio=bio_;
        //if(plantC>0) bio = new plantcomplex(plantC, 9.05561E-05, 3.01854E-05, 35, 500,barea*0.5);//whole area
        //else bio = new algae(0.01,0.0004813522,5,barea);//double in one day
		
	 	//numOutN=0;
		remove=0.0;
		remove_parcel=0;
		remove_bal=0.0;
        
        randtable = new int[1000];
        randlist = new double[1000];
        for(int i=0; i<1000; i++){
            randtmp=0.0;
            while(!(randtmp>0.0 && randtmp<1.0)) {randtmp = Math.random();}//System.out.printf(".");}
            randlist[i]=randtmp;
            randt = (double) ttmaxMean;
            randt *= -1;
            randt *= Math.log(1-randtmp);
            randtt = (int) Math.round(randt);
            if(randtt==0) randtt++;
            randtable[i] = randtt;
        }
        
	}
	public streamZone(String name_, int ttmaxMean_, bioProcessBlock bio_, streamZone next_){
        name = name_;
        
		income = new parcelList();
        incomeAlgaeC=0.0;
        incomeAlgaeN=0.0;
		hold = new parcelList();
		next = next_;
		ttmaxMean = ttmaxMean_;
		bio=bio_;
        //if(plantC>0) bio = new plantcomplex(plantC, 9.05561E-05, 3.01854E-05, 35, 500,barea*0.5);//per mgC/m2
	 	//else bio = new algae(0.01,0.0004813522,5,barea);
        
        //numOut=0;
		remove=0.0;
		remove_parcel=0;
		remove_bal=0.0;
        
        randtable = new int[1000];
        randlist = new double[1000];
        for(int i=0; i<1000; i++){
            randtmp=0.0;
            while(!(randtmp>0.0 && randtmp<1.0)) {randtmp = Math.random();}//System.out.printf(".");}
            randlist[i]=randtmp;
            randt = (double) ttmaxMean;
            randt *= -1;
            randt *= Math.log(1-randtmp);
            randtt = (int) Math.round(randt);
            if(randtt==0) randtt++;
            randtable[i] = randtt;
        }
        
	}
	
    
	public void setIncome(int num, int head){
        
        randtt = head;
        
//        System.out.printf("setIncome: ");
		for(int i=0; i<num; i++){
			income.add(new parcel(ttmaxMean,0) );//<<------------- travel time distribution
            
//            income.add(new parcel(randtable[randtt],0) ); randtt++; randtt%=1000;
            
		}
	}
	public void takeincome(parcel p, int head){
		p.tt=0;
		p.ttmax=ttmaxMean; //<<------------- travel time distribution
//        p.ttmax=randtable[head];
		income.add(p);
        
	}
	public void incomeTohold(){
        
        if(incomeAlgaeC>0){
            bio.add2AlgalC( (incomeAlgaeC-bio.getAlgalC())*income.len/(hold.len+income.len) );
            bio.add2AlgalN( (incomeAlgaeN-bio.getAlgalN())*income.len/(hold.len+income.len) );
        }
        
		if(income.len>0){
			hold.add(income);
			income.clean();	
		}
 
        
	}
	
	public void run() throws Exception{
        
        randtt = ref.randhead;
        numOutQ=0;
        numOutN=0;
        
        
        //determine biotic uptake = # parcel;
        if(ref.min<=ref.iniPeriod){remove=0;}else{remove = bio.run(hold.len,hold.nlen, ref.min,ref.day,ref.hour);}
        if( !ref.shortcut && remove > 0){
            
            //----------------------------------------------- calculate remove/uptake flux and convert to # parcel
            if(remove_bal>remove){ remove_bal-=remove; remove_parcel=0;}
            else if(remove_bal>0){
                remove-=remove_bal;
                remove_parcel = (int)(ref.flux2parcel*remove); if(ref.flux2parcel*remove-remove_parcel>1E-10) remove_parcel++;
                remove_bal = remove_parcel*ref.parcel2flux - remove; if(remove_bal>1E-10){}else{remove_bal=0;}
            }else{
                remove_parcel = (int)(ref.flux2parcel*remove); if(ref.flux2parcel*remove-remove_parcel>1E-10) remove_parcel++;
                remove_bal = remove_parcel*ref.parcel2flux - remove; if(remove_bal>1E-10){}else{remove_bal=0;}//<<---- must be positive
            }
            
            if(remove_parcel>hold.nlen) System.out.printf("[%s] err: remove(%4.12f,%d) > hold (%d)  ",name,remove*ref.flux2parcel,remove_parcel,hold.nlen);
            //-----------------------------------------------
            
            index = -1;
            step = ((double)hold.nlen)/remove_parcel; //System.out.printf("step = %f ",step); //[remove_parcel is int; hold.nlen is int]
            chosen = (int)(step*randlist[randtt]); randtt++; randtt%=1000;
            
            hold.seed();
            while(hold.next()){
                
//                if(hold.point.nitrogen){index++;}//<<----- counting nitrogen <<--- 2016: i think this should be here, but got error
                
                // Feb 2016, seems to have some issue here.
                if(hold.nlen>=remove_parcel && remove_parcel>0 && index==chosen && hold.point.nitrogen){
                    //biotic uptake
                    chosen += Math.max(1,(int)(step*randlist[randtt])); randtt++; randtt%=1000;
                    
                    //System.out.printf("%s,%d\n",name,hold.point.totaltt);
                    ref.Nremove_time+=hold.point.totaltt;
                    ref.Nremove_num++;
                    
                    hold.removeN();//leaving system //set point read for the next()
                    remove_parcel--;
                    index++;
                    //System.out.printf("[%d] ",hold.nlen);
                    //System.out.printf("(%d,%d- ",chosen,index);
                }
                if(hold.point.nitrogen){index++;}//<<----- counting nitrogen
                
                //increase time
                hold.point.tt++;
                hold.point.totaltt++;
                
                //transfer
                if(hold.point.tt > hold.point.ttmax){
                    numOutQ++;
                    if(hold.point.nitrogen) numOutN++;
                    if(next!=null){next.takeincome(hold.remove(),randtt); randtt++; randtt%=1000;}//<<---- send to downstream
                    else hold.remove();
                }//transfer
                //System.out.printf("%d) ",index);
            }//while
            
            if(next!=null){
                next.incomeAlgaeC = bio.getAlgalC();
                next.incomeAlgaeN = bio.getAlgalN();
            }
            
            //System.out.printf("\n");
            if(remove_parcel>0) System.out.printf("[%s] error: remove_parcel = %d > 0 ",name,remove_parcel);
        }else{
            hold.seed();
            while(hold.next()){
                
                //increase time
                hold.point.tt++;
                hold.point.totaltt++;
                
                //transfer
                if(hold.point.tt > hold.point.ttmax){
                    numOutQ++;
                    if(hold.point.nitrogen) numOutN++;
                    if(next!=null){next.takeincome(hold.remove(),randtt); randtt++; randtt%=1000;}
                    else hold.remove();
                }//transfer
            }//while
            if(next!=null){
                next.incomeAlgaeC=0.0;
                next.incomeAlgaeN=0.0;
            }
        }// remove > 0?
        
        
    }
 
	
	
}//class