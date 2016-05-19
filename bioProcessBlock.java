interface bioProcessBlock{
	double run(int waterp, int nitrogenp, int t, int d, int h);
	
    
    double getPlantC();
    double getAlgalC();
    double getAlgalN();
    double getDeadAlgalC();
    double getDeadAlgalN();
    void add2AlgalC(double cc);
    void add2AlgalN(double nn);
    
    double getPlantUptake();
    double getAlgalUptake();
    double getRemove();
    
}