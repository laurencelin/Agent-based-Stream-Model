import java.util.concurrent.Callable;

class mycallable implements Callable<Exception> {
    
    streamZone p;
    
    public mycallable(streamZone p_){
        p = p_;
    }
    
    //@Override
    public Exception call(){
        try{
            p.run();
            return null;
        }catch(Exception e){
            return e;
        }
    }
	
}