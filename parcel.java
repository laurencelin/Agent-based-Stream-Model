public class parcel {
	
	int tt;
	int totaltt;
	int ttmax;
    parcel next;
    parcel prev;
    
    //parcel itself is a small amount of water.
    
    
	int sourceID;
	//holding for
    boolean nitrogen;
    
	
    
	public parcel(int ttmax_, int sourceID_){
		tt=0;
		totaltt=0;
		ttmax = ttmax_;
		sourceID = sourceID_;
		next = null;
		prev = null;

        nitrogen = true;
	}
	
}

