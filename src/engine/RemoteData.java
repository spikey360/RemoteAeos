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

package engine;

import java.io.*;
import javax.bluetooth.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.Image;
import java.util.Vector;
import gui.AeosRemote;
import tools.*;

public final class RemoteData implements DiscoveryListener{
 //server uuid
 private static final UUID BTRMSERVERUUID=new UUID("ae05a5ea459d4225938528f7ee7fa848",false);
 //status codes for state of Engine
 private static final int READY=0;
 private static final int DEVICE_SEARCH=1;
 private static final int SERVICE_SEARCH=2;
 private static final int SEARCH_COMPLETE=3;
 private int state=READY;
 private byte psbuffer[]=new byte[1024*8]; //8 KB protocol string buffer
 private String name;
 private int size;
 //discovery agent
 private DiscoveryAgent discoveryAgent;
 //devices
 private Vector devices=new Vector();
 //services
 private Vector services=new Vector();
 //AeosRemoteGUI
 private AeosRemote parent;
 //thread to do all BT processes
 private Thread processorThread;
 //type of device
 private int discoveryType;
 //vector of commands available
 private Vector commands=new Vector();
 //flag to cancel image download
 private boolean cancelImageDownload;
 //uuid set
 private UUID[] uuidSet=new UUID[1];
 //connection to device
 private StreamConnection conn;
 private InputStream in;
 private OutputStream out;
 //the place to store all obtained operators
 private OpStore opstore;
 //the place to store all controls
 private OpStore constore;
 
 public  RemoteData(AeosRemote parent){
  this.parent=parent;
  uuidSet[0]=BTRMSERVERUUID;
  opstore=new OpStore();
  constore=new OpStore();
  run();
 }
 
 public void run(){
  boolean ready=false;
  try{
   //create local device
   LocalDevice localDevice=LocalDevice.getLocalDevice();
   discoveryAgent=localDevice.getDiscoveryAgent();
   //bluetooth is ready
   ready=true;
  }catch(Exception e){e.printStackTrace();}
  //no use running if bt cannot be started
  if(!ready){
   return;
  }
  //list all devices in piconet
  if(!searchDevicesInPiconet())
   return;
  
 }
 
 public void displayResults(){
  parent.appendToMainScreen("Results:");
  if(services.size()==0){
   parent.appendToMainScreen("No suitable service found");
   }else{
   parent.appendToMainScreen(services.size()+" service(s) found");
  for(int i=0;i<services.size();i++){
   try{
     parent.addToList(((ServiceRecord)services.elementAt(i)).getHostDevice().getFriendlyName(false));
    }catch(IOException e){
     e.printStackTrace();
    }
    devices.removeElement(services.elementAt(i));
    }
   }
   if(devices.size()>0){
    parent.appendToMainScreen(devices.size()+" other device(s) found..");
    for(int i=0;i<devices.size();i++){
     try{
     parent.addToList(((ServiceRecord)devices.elementAt(i)).getHostDevice().getFriendlyName(false));
     }catch(IOException e){
      e.printStackTrace();
      }
    }
   }
 }
 
 public void deviceDiscovered(RemoteDevice rmdev, DeviceClass dc){
  if(devices.indexOf(rmdev)==-1)
  	devices.addElement(rmdev);
 }
 
 public void inquiryCompleted(int devtype){
  
  discoveryType=devtype;
  parent.appendToMainScreen("Device inquiry completed");
  searchForRemoteService();
}
 
 public void servicesDiscovered(int transID, ServiceRecord[] servRecord){
  for(int i=0;i<servRecord.length; i++){
   //check if it is the service required for remote
   services.addElement(servRecord[i]);
  }
 }
 
 public void serviceSearchCompleted(int transID, int respCode){
  //do nothing
  /*synchronized(lock){
   lock.notify();
  }*/
  state=SEARCH_COMPLETE;
  parent.appendToMainScreen("Service search complete");
  displayResults();
 }
 
 void pullAvailableOptions(){
 
 }
 
 private boolean searchDevicesInPiconet(){
  state=DEVICE_SEARCH;
  try{
   //first visible devices
   parent.appendToMainScreen("Starting search...");
   discoveryAgent.startInquiry(DiscoveryAgent.GIAC,this);
   //hidden devices next
   //discoveryAgent.startInquiry(DiscoveryAgent.LIAC,this);
  }catch(BluetoothStateException bsx){
   System.err.println("Could not start inquiry");
   parent.appendToMainScreen("Could not start inquiry");
   return false;
   
  }
  
  switch(discoveryType){
   case INQUIRY_ERROR:
    return false;
   case INQUIRY_TERMINATED:
    return false;
   case INQUIRY_COMPLETED:
    break;
   default:
    System.err.println("Unexpected device discovery code");
    return false;
  }
  return true;
 }
 
 private void searchForRemoteService(){
  state=SERVICE_SEARCH;
  parent.appendToMainScreen("Starting service search");
  for(int i=0;i<devices.size();i++){
   RemoteDevice rd=(RemoteDevice)devices.elementAt(i);
   try{
    discoveryAgent.searchServices(null,uuidSet,rd,this);
   }catch(BluetoothStateException bsx){
    System.err.println("Error while searching for services");
   }
  }
 }
 
 public void establishConnection(int x){
 //x is the index of the service
 if(state!=SEARCH_COMPLETE) //search not completed yet
 	return;
 if(services.size()<=0) //no valid services
 	return;
 try{
 String url=((ServiceRecord)services.elementAt(x)).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT,false);
 conn=(StreamConnection)Connector.open(url);
 }catch(IOException e){
 	parent.appendToMainScreen("Could not establish connection");
 }
 parent.appendToMainScreen("Shaking hands..");
 byte nonce[]=new byte[512];
 int nl=0;
 try{
 in=conn.openInputStream();
 out=conn.openOutputStream();
 }catch(IOException e) {
 	parent.appendToMainScreen("Error while opening stream");
 }
 try{
 nl=in.read(nonce,0,nonce.length);
 parent.appendToMainScreen("Nonce received: "+nl+"B");
 out.write(nonce,0,nl);
 out.flush(); //one of the most important routines
 
 parent.appendToMainScreen("Shaken hands successfully");
 }catch(IOException e) {
 	parent.appendToMainScreen("Error during handshaking");
 	closeConnection();
 	parent.appendToMainScreen("Connection Terminated");
 }finally{
 //closeConnection(); //not necessary unless the server implements so too
 }
 }
 
 public void readProtocolString(){
 try{

 int len=0;
 len=in.read(psbuffer,0,psbuffer.length);
 String ps=new String(psbuffer,0,len);
 String opts[]=Tools.split(ps,';');
 if(opts[0].equals("l")){
	for(int i=1;i<opts.length-1;i++){
		parent.addOperation(opstore.storeOp(opts[i]));
		}
	parent.appendToMainScreen("Operation list received");
 }
 else if(opts[0].equals("c")){
 	//parent.appendToMainScreen("Reading control list");
 	for(int i=1;i<opts.length-1;i++){
		parent.addControl(constore.storeOp(opts[i]));
		}
	parent.appendToMainScreen("Control list received");
 }
 else if(opts[0].equals("p")){
 /////////////////////////////
 String z="";
 for(int i=1;i<opts.length-1;i++)
 	z+=opts[i]+"\n";
 /////////////////////////////
 parent.showInformation(z);
 }
 else if(opts[0].equals("ackx")){
 //done, the picture has been displayed
 //add functionality later
 }
 else if(opts[0].equals("ackc")){
 //done, the control has been executed
 //add functionality later
 }
 else{
 //to be interpreted as picture, implementation later
 }
 }catch(IOException e){
 	parent.appendToMainScreen("Error while reading protocol string");
 	//close connection
 	closeConnection();
 	parent.appendToMainScreen("Connection terminated");
 }
 finally{
 //closeConnection();
 }
 }
 
 public void requestInfo(String z){
 try{
 String cap=opstore.getOpcode(z);
 String msg="d;"+cap;
 int len=0;
 out.write(msg.getBytes());
 out.flush();
 readProtocolString();
 }catch(IOException e){
 parent.appendToMainScreen("Error during seeking info");
 e.printStackTrace();
 }
 }
 
 public void requestExec(String z){
 try{
 String cap=opstore.getOpcode(z);
 String msg="x;"+cap;
 int len=0;
 out.write(msg.getBytes());
 out.flush();
 readProtocolString();
 }catch(IOException e){
 parent.appendToMainScreen("Error during operation request");
 e.printStackTrace();
 }
 }
 
 public void requestControl(String z){
 try{
 String cap=constore.getOpcode(z);
 String msg="c;"+cap;
 int len=0;
 out.write(msg.getBytes());
 out.flush();
 readProtocolString();
 }catch(IOException e){
 parent.appendToMainScreen("Error during control request");
 e.printStackTrace();
 }
 }
 
 public void sendAck(String z){
 try{
 //String cap=constore.getOpcode(z);
 String msg="ack"+z+";";
 int len=0;
 out.write(msg.getBytes());
 out.flush();
 //readProtocolString();
 }catch(IOException e){
 parent.appendToMainScreen("Acknowledgement failed");
 e.printStackTrace();
 }
 }
 
 public Image loadImage(String z){
 //expecting a picture now

 int n=0;
 try{
 String cap=opstore.getOpcode(z);
 String msg="m;"+cap;
 int len=0;
 out.write(msg.getBytes());
 out.flush();
 //read
 //int len=0;
 len=in.read(psbuffer,0,psbuffer.length);
 String ps=new String(psbuffer,0,len);
 String opts[]=Tools.split(ps,';');
 name=opts[1];
 size=Integer.parseInt(opts[2]);
 byte picdata[]=new byte[size];
 parent.appendToMainScreen("Fetching "+name+" of size "+size+"B");
 //send req for picture
 msg="f;"+cap;
 out.write(msg.getBytes());
 out.flush();
 
 while(n!=size){
 	len=in.read(picdata,n,picdata.length-n); //read image completely
 	n+=len;
 	if(len==-1)
 		throw new IOException("Could not read image completely");
 }
  if(n!=size)
  	throw new IOException("Could not read image completely");
  return Image.createImage(picdata,0,picdata.length);
 }catch(IOException e){
  parent.appendToMainScreen("Error while reading image:"+n+"B read");
  e.printStackTrace();
  closeConnection();
  parent.appendToMainScreen("Connection terminated");
 }
 finally{
 closeConnection();
 return null; //if all fails
 
 }
 }
 
 public void closeConnection(){
  try{
  //quitting
  String msg="q;"+"\0";
  out.write(msg.getBytes());
  out.flush(); 
  }catch(IOException e){
  e.printStackTrace();
  }finally{
   try{
    	in.close();
  	out.close();
   }catch(IOException e){
   	e.printStackTrace();
   }
  }
 }
 
}
