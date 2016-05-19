class blank implements bioProcessBlock{
	
	public blank(){ }//
	
	public double run(int waterp, int nitrogenp, int t, int d, int h){
		return 0.0;
	}
	
    public double getPlantC(){return 0.0;}
    public double getAlgalC(){return 0.0;}
    public double getAlgalN(){return 0.0;}
    public double getDeadAlgalC(){return 0.0;}
    public double getDeadAlgalN(){return 0.0;}
    public void add2AlgalC(double cc){}
    public void add2AlgalN(double nn){}
    
    
    public double getPlantUptake(){return 0.0;}
    public double getAlgalUptake(){return 0.0;}
    public double getRemove(){return 0.0;}
}