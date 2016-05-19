import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class modelv03 {

	public static void main(String[] args) throws Exception {
        
        ref.Nremove_time=0.0;
        ref.Nremove_num=0;
        
        cmdParser parser = new cmdParser();
        parser.intOption.put("cpu",7);
        parser.intOption.put("ini",1440);
        parser.intOption.put("day",100);
       
        parser.flag.put("-deb",false);
        parser.flag.put("-shortcut",false);
        parser.txtOption.put("pre", "set1_");
        parser.txtOption.put("seqType", "p-p"); //<---- type
        parser.txtOption.put("seqTime", "1-1"); //<---- travel time
        parser.txtOption.put("seqArea", "1.0-1.0"); //<---- benthic area
        parser.txtOption.put("seqDep", "1.0-1.0"); //<---- depth
        
        parser.doubleOption.put("nfrac",1.0);
        parser.doubleOption.put("par",10000.0);// # of number use for simulation
        parser.doubleOption.put("qvol",660.0);// total vol of water in one time step (min)
        parser.doubleOption.put("nflux",266340.0); //mg per min (default)
       
    
        //Mette et al. 2011
        //phytoplankton molar C:N ~ 9.5 (seston CN) => mass C:N = 8.142857 for initial
        //low molar C:N 5.873 ~ mass C:N 5.034
        //high molar C:N 14.242 ~ mass C:N 12.20743
        //Dickman et al. 2008 phytoplankton cell stoichiometry (molar?)
        //CN 6.866 - 12.297
        parser.doubleOption.put("phyIni",50.0);//50 //initial mass //19451; mgC/m3 <== Havens, Beaver, East - 2007 phytoplankton 298(211-419)-378(289-467)-734(533-976) mgC/m3
        parser.doubleOption.put("phyGMAX",0.003472222); // 1/min
        parser.doubleOption.put("phyReduce",0.00123); // 1/min
        parser.doubleOption.put("phyCN",8.142857);
        parser.doubleOption.put("phyqCN",12.20743);
        parser.doubleOption.put("phyUmax",0.0003680556); //mgN/mgC/min
        parser.doubleOption.put("phyKH",14.0); //mgN/m3 = ugN/L
        parser.doubleOption.put("phyKS",0.0015); //mgN/m3 = ugN/L
        
        parser.doubleOption.put("GMAX",2.179167e-05);// 1/min
        parser.doubleOption.put("stemIni",12.67433);//gC/m2
        parser.doubleOption.put("stemGMAX",2.370139e-05);// 1/min
        parser.doubleOption.put("stopGMAX",104.0);// days
        parser.doubleOption.put("stemCN",12.65610652);
        parser.doubleOption.put("rootIni",43.47648);//gC/m2
        parser.doubleOption.put("rootCN",22.52218347);//gC/m2
        parser.doubleOption.put("rootStemFr",1.479);//
        parser.doubleOption.put("coverIni",0.2215);//%

        
        parser.parse(args);
        ref.shortcut = parser.flag.get("-shortcut");
        
        ref.GMAX = parser.doubleOption.get("GMAX");
        ref.root_stemFr = parser.doubleOption.get("rootStemFr");
        ref.stemGMAX = parser.doubleOption.get("stemGMAX");
        ref.stopGMAX = parser.doubleOption.get("stopGMAX");
        ref.stemCN = parser.doubleOption.get("stemCN");
        ref.stemNC = 1.0/ref.stemCN;
        ref.rootCN = parser.doubleOption.get("rootCN");
        ref.rootNC = 1.0/ref.rootCN;
        ref.plantNperC = ref.stemNC + ref.root_stemFr*ref.rootNC;
        
        ref.algalGMAX = parser.doubleOption.get("phyGMAX");
        ref.algalReduce = 1.0 - parser.doubleOption.get("phyReduce");
        ref.algalCN = parser.doubleOption.get("phyCN");
        ref.algalNC = 1.0/ref.algalCN;
        ref.algalqCN = parser.doubleOption.get("phyqCN");
        ref.algalqNC = 1.0/ref.algalqCN;
        ref.algalUmax = parser.doubleOption.get("phyUmax");
        ref.algalKH = parser.doubleOption.get("phyKH");
        ref.algalKS = parser.doubleOption.get("phyKS");
        
        System.out.print("got-seqType:"+parser.txtOption.get("seqType")+"\n");
        System.out.print("got-seqTime:"+parser.txtOption.get("seqTime")+"\n");
        System.out.print("got-seqArea:"+parser.txtOption.get("seqArea")+"\n");
        System.out.print("got-seqDep:"+parser.txtOption.get("seqDep")+"\n");
        
		int NumOfProcessors=parser.intOption.get("cpu");
        String pref = parser.txtOption.get("pre");
        boolean debug = parser.flag.get("-deb");
        double nfrac = parser.doubleOption.get("nfrac");
        
        ExecutorService es = Executors.newFixedThreadPool(NumOfProcessors);
        List<Callable<Exception>> ProcessQ;//
        List<Future<Exception>> SafeCheck;//
        

        ref.NumOfParcel = (int) ((double)parser.doubleOption.get("par"));
		ref.flux2parcel = parser.doubleOption.get("par")/parser.doubleOption.get("nflux")/nfrac; //# parcel for 1 mgN
		ref.parcel2flux = 1.0/ref.flux2parcel;//mgN per parcel
		ref.Q2parvel = parser.doubleOption.get("par")/parser.doubleOption.get("qvol"); //# parcel for 1 m3
		ref.parcel2Q = 1.0/ref.Q2parvel; //m3 per parcel
		
		ref.iniPeriod = parser.intOption.get("ini");//1440;
		ref.simPeriod = ref.iniPeriod+ parser.intOption.get("day")*1440;
        

        int NumOfstreamZone;
        double[] removeFlux;
        double[] plantUptakeFlux;
        double[] algalUptakeFlux;
        streamZone[] stream;
        
        
        StringTokenizer gtoken = new StringTokenizer(parser.txtOption.get("seqType"),"-");
        List<Integer> seqType = new ArrayList<Integer>();
        String holder;
//        System.out.printf("begin seqType:\n"); //for testing
        while (gtoken.hasMoreTokens()) {
            holder = gtoken.nextToken();
//            System.out.printf("%s___ ",holder); System.out.print( (holder == "p") +"\n"); //for testing
            if(holder.equals("p")){
                seqType.add(1); // 1 = pool
            }else if (holder.equals("b")){
                seqType.add(2); // 2 = bed
            }else{
                seqType.add(0); // 0
            }
        }
        
        gtoken = new StringTokenizer(parser.txtOption.get("seqTime"),"-");
        List<Integer> seqTime = new ArrayList<Integer>();
        while (gtoken.hasMoreTokens()) {
            seqTime.add(Integer.parseInt(gtoken.nextToken()) );
        }
        
        gtoken = new StringTokenizer(parser.txtOption.get("seqArea"),"-");
        List<Double> seqArea = new ArrayList<Double>();
        while (gtoken.hasMoreTokens()) {
            seqArea.add(Double.parseDouble(gtoken.nextToken()) );
        }
        
        gtoken = new StringTokenizer(parser.txtOption.get("seqDep"),"-");
        List<Double> seqDep = new ArrayList<Double>();
        while (gtoken.hasMoreTokens()) {
            seqDep.add(Double.parseDouble(gtoken.nextToken()) );
        }

        System.out.printf("size = %d\n",seqTime.size());
        
        double bedarea=0.0, poolarea=0.0;
        for(int i=0; i<seqTime.size(); i++){
            if(seqType.get(i)==1){poolarea+=seqArea.get(i);}
            else{bedarea+=seqArea.get(i);}
        }
        
        System.out.printf("bedsize = "+bedarea+" pool = "+poolarea+"\n");
        
        /*
         streamZone is the zone in the model diagram.
         it contains:
            1) income parcels,
            2) holding parcels,
            3) its downstream zone (stream unit)
            4) ttmaxMean
            5) bio process submodels
         */
        /*
         parcel objection contains:
            1) tt = current residence time (time step) within a zone
            2) totaltt = total residence time for this parcel staying within a stream segment
            3) ttmax = max time (step) for this parcel to stay in this zone
            4) sourceID (not really used for now)
            
            next, prev are links to the neighbour parcels in the parcelList
            parcelList is a customized ArrayList
         
         */
        /*
         mycallable is an object to warp around the streamZone (trigger streamZone.run()) for parallel runs
         */
        
        
        /// setting up the model based on the input interface
        NumOfstreamZone=seqTime.size();
        ProcessQ = new ArrayList<Callable<Exception>>(NumOfstreamZone);
        stream = new streamZone[NumOfstreamZone];
        removeFlux = new double[NumOfstreamZone];
        plantUptakeFlux = new double[NumOfstreamZone];
        algalUptakeFlux = new double[NumOfstreamZone];
        for(int j=0; j<NumOfstreamZone; j++){
            removeFlux[j]=0;
            plantUptakeFlux[j]=0;
            algalUptakeFlux[j]=0;
            
            holder = "s"+j+"_"+seqType.get(j);
            if(seqType.get(j)==0){
                // blank (upstream holder)
                stream[j] = new streamZone(holder, seqTime.get(j), new blank());
            }else if(seqType.get(j)==1){
                // pool-dominated zone
                stream[j] = new streamZone(holder, seqTime.get(j), new plantcomplex(0.0, 0.0,0.0, parser.doubleOption.get("phyIni"),seqArea.get(j),seqDep.get(j) ));
            }else if(seqType.get(j)==2){
                // bedrock shoal dominated zone
                stream[j] = new streamZone(holder, seqTime.get(j), new plantcomplex(parser.doubleOption.get("stemIni"),parser.doubleOption.get("rootIni"),parser.doubleOption.get("coverIni"), parser.doubleOption.get("phyIni"),seqArea.get(j),seqDep.get(j) ));
            }
            
            ProcessQ.add(new mycallable(stream[j]) );
            stream[j].setIncome(ref.NumOfParcel,0);
        }//<-------
         for(int j=0; j<NumOfstreamZone-1; j++){
             stream[j].next = stream[j+1];
         }
   

        //------ remove,uptake ------ time series (row), zone (col)
        BufferedWriter toOUTPUT_flux = new BufferedWriter(new FileWriter(parser.txtOption.get("pre")+"result_flux.csv"));//buffer
		holder = parser.txtOption.get("seqType");
        holder = holder.replace("b-","b,d,");
        holder = holder.replace("p-","p,NA,");
		toOUTPUT_flux.write("time,day,hour,"+holder+"\n");
        
        //------ n/q ------ time series (row), zone (col)
        BufferedWriter toOUTPUT_conc = new BufferedWriter(new FileWriter(parser.txtOption.get("pre")+"result_nq.csv"));//buffer <<------------------- use for analysis
		toOUTPUT_conc.write("time,day,hour,"+holder+"\n");
        
        //------ n ------ time series (row), zone (col)
        BufferedWriter toOUTPUT_n = new BufferedWriter(new FileWriter(parser.txtOption.get("pre")+"result_n.csv"));//buffer <<------------------- use for analysis
        toOUTPUT_n.write("time,day,hour,"+holder+"\n");
        
        //------ q ------ time series (row), zone (col)
        BufferedWriter toOUTPUT_q = new BufferedWriter(new FileWriter(parser.txtOption.get("pre")+"result_q.csv"));//buffer <<------------------- use for analysis
        toOUTPUT_q.write("time,day,hour,"+holder+"\n");
        
        //------ "biomass" in each zone  ------ time series (row), zone (col)
		BufferedWriter toOUTPUT_bio = new BufferedWriter(new FileWriter(parser.txtOption.get("pre")+"result_biomass.csv"));//buffer
		toOUTPUT_bio.write("time,day,hour,"+holder+"\n");

        
		
		//let say t is minute. then one day is 24*60 = 1440; one month = 43200
        ref.day=0; double day_1 = Math.pow(1440,-1);
        ref.hour=0; double hour_1 = Math.pow(60,-1);
        ref.randhead=0;
        int printindex=0;
        double dailyPlantUptake=0.0, dailyDenRemove=0.0, dailyAlgalUptake=0.0; //whole channal
        double accuPlantUptake=0.0, accuDenRemove=0.0, accuAlgalUptake=0.0; //whole channel
        
		for(ref.min=0; ref.min<=ref.simPeriod; ref.min++){
            
			
            //upstream input
            ref.day = (int)(ref.min*day_1);
            ref.hour = (int)(ref.min*hour_1); ref.hour%=24;
            ref.randhead = (int)(1000*Math.random()); if(ref.randhead==1000) ref.randhead--;
			
            if(!ref.shortcut){stream[0].setIncome(ref.NumOfParcel,ref.randhead);}//<<------ set upstream input
            SafeCheck = es.invokeAll(ProcessQ); //<-----------------
            for(Future<Exception> v: SafeCheck){ if(v.get()!=null){throw v.get();}} //<<----- execute streamZone.run() parallelly
            
            
            // time loop
            if(ref.min>ref.iniPeriod-1){
                printindex%=1440;
                
                //printing (daily) time
                if(printindex==0){
                    toOUTPUT_flux.write(ref.min+","+ref.day+","+ref.hour+",");
                    toOUTPUT_conc.write(ref.min+","+ref.day+","+ref.hour+",");
                    toOUTPUT_n.write(ref.min+","+ref.day+","+ref.hour+",");
                    toOUTPUT_q.write(ref.min+","+ref.day+","+ref.hour+",");
                    toOUTPUT_bio.write(ref.min+","+ref.day+","+ref.hour+",");
                }
                
                // every time step and zone
                for(int j=0; j<NumOfstreamZone; j++){
                    plantUptakeFlux[j]+=stream[j].bio.getPlantUptake();
                    algalUptakeFlux[j]+=stream[j].bio.getAlgalUptake();
                    removeFlux[j]+=stream[j].bio.getRemove();  //<<--- functions are in min scale, but aggregrating daily here
                    
                    
                    dailyPlantUptake+=plantUptakeFlux[j];
                    dailyDenRemove+=removeFlux[j];
                    dailyAlgalUptake+=algalUptakeFlux[j];
                    
                    accuPlantUptake+=plantUptakeFlux[j];
                    accuDenRemove+=removeFlux[j];
                    accuAlgalUptake+=algalUptakeFlux[j];

                    //printing (daily) variable
                    if(printindex==0){
                        if(j<NumOfstreamZone-1){
                            
                            toOUTPUT_flux.write(algalUptakeFlux[j]+","+plantUptakeFlux[j]+","+removeFlux[j]+",");//dailyflux
                            plantUptakeFlux[j]=0;
                            removeFlux[j]=0;
                            algalUptakeFlux[j]=0;
                            
                            toOUTPUT_conc.write( (stream[j].hold.nlen*ref.parcel2flux)/(stream[j].hold.len*ref.parcel2Q) +","+ (stream[j].numOutN*ref.parcel2flux)/(stream[j].numOutQ*ref.parcel2Q) +",");  /////<<----------- N conc[] within zone, N conc[] flow out zone to downstream
                            toOUTPUT_n.write( (stream[j].hold.nlen*ref.parcel2flux) +","+ (stream[j].numOutN*ref.parcel2flux) +",");
                            toOUTPUT_q.write( (stream[j].hold.len*ref.parcel2Q) +","+ (stream[j].numOutQ*ref.parcel2Q) +",");
                            
                            toOUTPUT_bio.write(stream[j].bio.getAlgalC()+","+stream[j].bio.getAlgalN()+","+stream[j].bio.getDeadAlgalC()+","+stream[j].bio.getDeadAlgalN()+","+stream[j].bio.getPlantC()+","); //already in density per unit area
                            
                        }else{
                            
                            toOUTPUT_flux.write(algalUptakeFlux[j]+","+plantUptakeFlux[j]+","+removeFlux[j]+","+dailyAlgalUptake+","+dailyPlantUptake+","+dailyDenRemove+","+accuAlgalUptake+","+accuPlantUptake+","+accuDenRemove+"\n");
                            plantUptakeFlux[j]=0;
                            removeFlux[j]=0;
                            algalUptakeFlux[j]=0;
                            
                            toOUTPUT_conc.write( (stream[j].hold.nlen*ref.parcel2flux)/(stream[j].hold.len*ref.parcel2Q) +","+ (stream[j].numOutN*ref.parcel2flux)/(stream[j].numOutQ*ref.parcel2Q) +"\n");
                            toOUTPUT_n.write( (stream[j].hold.nlen*ref.parcel2flux) +","+ (stream[j].numOutN*ref.parcel2flux) +"\n");
                            toOUTPUT_q.write( (stream[j].hold.len*ref.parcel2Q) +","+ (stream[j].numOutQ*ref.parcel2Q) +"\n");
                            
                            toOUTPUT_bio.write(stream[j].bio.getAlgalC()+","+stream[j].bio.getAlgalN()+","+stream[j].bio.getDeadAlgalC()+","+stream[j].bio.getDeadAlgalN()+","+stream[j].bio.getPlantC()+"\n");
                            dailyPlantUptake=0.0; dailyDenRemove=0.0; dailyAlgalUptake=0.0; ////<<--------?
                            
                            ///----------
                            System.out.printf("%d,%d,%d,%f,%d,%d\n",ref.min,ref.day,ref.hour,ref.Nremove_time,ref.Nremove_num,stream[j].numOutN);
                            ref.Nremove_time=0.0;
                            ref.Nremove_num=0;
                        }
                    }//print
                    
                }//end of j for loop
                accuPlantUptake=0.0; accuDenRemove=0.0; accuAlgalUptake=0.0; ////<<--------?
                printindex++;
            }//
            
			for(int j=0; j<NumOfstreamZone; j++){
                //transferring ***
                stream[j].incomeTohold();
            }
            
            if(ref.day%10==0){
                toOUTPUT_flux.flush();
                toOUTPUT_conc.flush();
                toOUTPUT_n.flush();
                toOUTPUT_q.flush();
                toOUTPUT_bio.flush();
            }
			if(debug) System.out.printf("time cycle %d is done.\n",ref.min);
			//System.out.printf("-----------------------------------\n");
			
		}//t
        
        ProcessQ.clear();
        es.shutdown();
        //System.out.printf("time = %f, num=%d\n",ref.Nremove_time, ref.Nremove_num);
        
        
		toOUTPUT_flux.close();
        toOUTPUT_conc.close();
        toOUTPUT_n.close();
        toOUTPUT_q.close();
		toOUTPUT_bio.close();
 
 
	}//main
	

	
}//class