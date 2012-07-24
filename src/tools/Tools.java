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
