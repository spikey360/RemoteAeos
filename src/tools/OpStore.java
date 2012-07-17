package tools;

import java.util.Hashtable;

public class OpStore{
private Hashtable store;
public OpStore(){
store=new Hashtable();
}
public String storeOp(String x){
	//x in the format <name>:<code>
	int k=x.indexOf(':');
	if(k==-1){
	//Typically an exception should be thrown
	return "-";
	}
	String name=x.substring(0,k);
	String code=x.substring(k+1);
	//make <code> the value and <name> the key
	store.put(name,code);
	//return the key
	return name;
}
public String getOpcode(String name){
	return (String)store.get(name);
}
}
