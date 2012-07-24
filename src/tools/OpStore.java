/*
RemoteAeos
======
    An app, which works in tandem with AeosPy running on desktop to provide remote control functionality over Bluetooth.
    Copyright (C) 2012  Spikey360

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    You can mail me at spikey360@yahoo.co.in
*/

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
