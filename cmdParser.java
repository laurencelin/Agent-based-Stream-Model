
import java.util.Map;
import java.util.HashMap;

public class cmdParser {
	/*
	 * args[] from main function (splited by space)
	 * -a 7.8 -t test.txt
	 * [-a], [7.8], [-t] [test.txt]
	 */
	
	public Map<String,Boolean> flag;
	public Map<String,String> txtOption;
	public Map<String,Integer> intOption;
	public Map<String,Double> doubleOption;
	
	public cmdParser(){
		flag = new HashMap<String,Boolean>();   	/*e.g., -r -f */
		txtOption = new HashMap<String,String>();	/*e.g., input=filename */
		intOption = new HashMap<String,Integer>();	/*e.g., npt=4 */
		doubleOption = new HashMap<String,Double>();/*e.g., growthrate=0.98 */
	}
	
	public void parse(String[] cmds){
		try{
			for(int i=0; i<cmds.length; i++){
				//System.out.print(cmds[i]+"(in)->");//debug
				
                if(cmds[i].contains("=")){
					//options
					
					//System.out.print(cmds[i]+"(=)->");//debug
					String[] options = cmds[i].split("=");
					
					//System.out.print("(split)->");//debug
					if(options.length==2){
						//System.out.print(options[0]+"+"+options[1]+"\n");//debug
						if(txtOption.containsKey(options[0])) txtOption.put(options[0], options[1]);
						if(intOption.containsKey(options[0])) intOption.put(options[0], Integer.parseInt(options[1]));
						if(doubleOption.containsKey(options[0])) doubleOption.put(options[0], Double.parseDouble(options[1]));
					}
				}
				else if(cmds[i].contains("-")){
					//flag
                    //System.out.print("flag");//debug
					if(flag.containsKey(cmds[i])){flag.put(cmds[i],true);}
				}
                else throw new Exception("args are wrong");
				//System.out.print("\n");//debug
			}//end of for loop
			
//			System.out.print("\n");
//			System.out.println(flag);
//			System.out.println(txtOption);
//			System.out.println(intOption);
//			System.out.println(doubleOption);
			
		}catch (NumberFormatException e) {
	        System.err.println("Require numbers");
	        System.exit(1);
	    }catch (Exception e) {
	        System.err.println(e);
	        System.exit(1);
	    }
	}
	
	

}

