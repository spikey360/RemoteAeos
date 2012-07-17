package tools;

public class Tools{
public static String[] split(String f, char c){
        //count the number of splits first
        int z=0; int in=0;
        String k=new String(f);
        while(in!=-1){
                if((in=k.indexOf(c))>=0){
                	//System.out.print(in+".");
                        z++; //found one
                        if(in<(k.length()-1)){
                        k=k.substring(in+1);
                        }
                }
               
        }
        //decided the array length
        //System.out.println(f+">>z="+z);
        String x[]=new String[z+1];
        for(int i=0;i<x.length;i++){
        	int d=f.indexOf(c);
        	if(d<0)
        	 d=f.length();
        	//System.out.println(f.indexOf(c)+">>"+f.substring(0,d));
                x[i]=f.substring(0,d);
                if(i==x.length-1) break;
                f=f.substring(d+1);
                //System.out.println("\t"+f);
        }
        return x;
 
 }
}
