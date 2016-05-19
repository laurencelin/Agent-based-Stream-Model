public class parcelList{
	parcel seed;  /// this is never removed.
    parcel end;
	parcel point;
	parcel tmp;
	int len;
    int nlen;
	
	public parcelList(){
		seed = new parcel(0,-99);
		end = seed;
        point = null;
		tmp = null;
		len=0;
        nlen=0;
	}
	
	public parcelList(parcel p){
		seed = new parcel(0,-99);
		seed.next = p;
		end = p;
		point = p;
		tmp = null;
		len=1;
        if(p.nitrogen){ nlen=1;}else{ nlen=0;}
        
	}
	
	
	// no construction or destruction
	public void add(parcel p){
		end.next = p;
		end.next.prev = end;
		end = end.next;
		end.next=null;
		len++;
        if(p.nitrogen){nlen++;}
	}
	public void add(parcelList pl){
		end.next = pl.seed.next;
		end.next.prev = end;
		end = pl.end;
		end.next=null;
		len+=pl.len;
        
        nlen+=pl.nlen;
	}
	
	public boolean next(){
		if(point.next!=null){point = point.next; return true;}else{return false;}
	}
	public boolean prev(){
		if(point.prev!=null){point = point.prev; return true;}else{return false;}
	}
	public boolean isNext(){
		if(point.next!=null){return true;}else{return false;}
	}
	public boolean isPrev(){
		if(point.prev!=null){return true;}else{return false;}
	}

    
	public void seed(){
		point = seed;
	}
	
	
	public parcel remove(){//need to work
		parcel p = null;
		
		if(point.prev == null){System.out.printf("remove seed!!\n"); }//point to seed
		else if(point.next==null){
			//System.out.printf("remove end!!...\n"); 
			p = point;
			point = point.prev; 
			end = point;
			point.next=null; len--; if(p.nitrogen) nlen--;
        }
		else{
			//middle
			p = point;
			tmp = point.next;
			point = point.prev;
			point.next = tmp;
			tmp.prev = point;
			len--;
            if(p.nitrogen) nlen--;
		}
		//set point to prev
		return p;
	}
    public parcel removeN(){
		
		if(point.prev == null){System.out.printf("remove seed!!\n"); }//point to seed
		else if(point.next==null){
			//System.out.printf("remove end!!...\n");
            point.nitrogen=false;
            nlen--;}
		else{
			//middle
            point.nitrogen=false;
            nlen--;
		}
		//set point to prev
		return point;
	}
	public void clean(){
		seed.next=null;
		end=seed;
		point=null;
		tmp=null;
		len=0;
        nlen=0;
        
	}
}
