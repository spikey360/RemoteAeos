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

package gui;

import engine.RemoteData;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

public final class AeosRemote extends MIDlet implements CommandListener{
private final Command mainScreenBack=new Command("Back",Command.BACK,2);
private final Command mainScreenFromOpList=new Command("Back", Command.BACK,2);
private final Command mainScreenFromConList=new Command("Back", Command.BACK,2);
private final Command gotoDevList=new Command("Devices",Command.ITEM,2);
private final Command exitCommand=new Command("Exit",Command.EXIT,2);
private final Command startSearch=new Command("Search", Command.OK,1);
private final Command gotoOpList=new Command("Operations",Command.ITEM,3);
private final Command gotoConList=new Command("Controls",Command.ITEM,4);
private final Command preview=new Command("Preview",Command.ITEM,4);
private final Command nextOp=new Command("Next",Command.ITEM,1);
private final Command showOpInfo=new Command("Info",Command.ITEM,2);
private final Command closeServerConn=new Command("Disconnect",Command.ITEM,2);
private final Form mainScreen=new Form("JaCoB");
private final String[] elem=new String[]{};
private Image opImage=null;
private Image servImage=null;
private Image servconnImage=null;
private RemoteData rem;
//list of devices found
private final List devList=new List("JaCoB servers",List.IMPLICIT,elem,null);
//list of operations available
private final List opList=new List("Operations",List.IMPLICIT,elem,null);
//list of controls available
private final List conList=new List("Controls",List.IMPLICIT,elem,null);
public AeosRemote(){
try{
opImage=Image.createImage("/images/op.png");
servImage=Image.createImage("/images/server.png");
servconnImage=Image.createImage("/images/server-conn.png");
}catch(IOException e){
e.printStackTrace();
}

///////////////////////////////////
mainScreen.addCommand(startSearch);
mainScreen.addCommand(exitCommand);
mainScreen.addCommand(gotoDevList);
mainScreen.addCommand(gotoOpList);
//mainScreen.addCommand(gotoConList);
mainScreen.setCommandListener(this);
devList.addCommand(mainScreenBack);
devList.addCommand(closeServerConn);
devList.setCommandListener(this);
opList.addCommand(mainScreenFromOpList);
opList.addCommand(showOpInfo);
//opList.addCommand(preview);
opList.addCommand(nextOp);
opList.addCommand(gotoConList);
opList.setCommandListener(this);
conList.addCommand(mainScreenFromConList);
conList.setCommandListener(this);
}
public void startApp(){
show(mainScreen);
try{
mainScreen.append(new ImageItem("Java album Control over Bluetooth",Image.createImage("/images/JACOBbanner.png"),ImageItem.LAYOUT_CENTER,"Java album Control over Bluetooth"));
}catch(IOException e){
e.printStackTrace();
}
}
protected void destroyApp(boolean unconditional){
rem.closeConnection();
}
protected void pauseApp(){}
public void commandAction(Command c, Displayable d){
if(c==exitCommand){
//destroy app
//destroyApp();
notifyDestroyed();
return;
}
if(c==startSearch){
rem=new RemoteData(this);
//clear device list
devList.deleteAll();
opList.deleteAll();
conList.deleteAll();
return;
}
if(c==gotoDevList){
show(devList);
return;
}
if(c==gotoOpList){
show(opList);
return;
}
if(c==gotoConList){
show(conList);
return;
}
if(d==devList){
 show(mainScreen);
 if(c==closeServerConn){
  Runnable runner=new Runnable(){
  public void run(){
  rem.closeConnection();
  opList.deleteAll();
  conList.deleteAll();
  //set connected image
  devList.set(devList.getSelectedIndex(),devList.getString(devList.getSelectedIndex()),servImage);
  
  }
 };
 new Thread(runner).start();
 return;
 }
 else{
 //must be select
 Runnable runner=new Runnable(){
 public void run(){
  rem.establishConnection(devList.getSelectedIndex());
  //read for operation list
  rem.readProtocolString();
  rem.sendAck("l");
  //read for control list
  rem.readProtocolString();
  rem.sendAck("c");
  //set connected image
  devList.set(devList.getSelectedIndex(),devList.getString(devList.getSelectedIndex()),servconnImage);
  
  }
 };
 new Thread(runner).start();
 }
 return;
}
if(d==opList){

if(c==mainScreenFromOpList){
show(mainScreen);
}
else if(c==showOpInfo){
Runnable runner=new Runnable(){
public void run(){
String z=opList.getString(opList.getSelectedIndex());
rem.requestInfo(z);
}
};
new Thread(runner).start();
}else if(c==preview){
String z=opList.getString(opList.getSelectedIndex());
Image i=rem.loadImage(z);
if(i==null){
Alert m=new Alert("Failed");
m.setType(AlertType.ERROR);
m.setString("Could not load image");
m.setTimeout(1500);
show(m);
return;
}else{
Alert m=new Alert("Preview");
m.setType(AlertType.INFO);
m.setImage(i);
m.setTimeout(1500);
show(m);
return;
}
}else if(c==nextOp){
Runnable runner=new Runnable(){
public void run(){
int n=opList.getSelectedIndex();
if(opList.getSelectedIndex()<(opList.size()-1))
	n++;
opList.setSelectedIndex(n,true);
String z=opList.getString(n);
rem.requestExec(z);
//rem.reqPic
}
};
new Thread(runner).start();
return;
}
else{
//select button
Runnable runner=new Runnable(){
public void run(){
String z=opList.getString(opList.getSelectedIndex());
rem.requestExec(z);
//rem.reqPic
}
};
new Thread(runner).start();
}
return;
}
if(d==conList){
if(c==mainScreenFromConList){
show(opList);
}else{
//selection
Runnable runner=new Runnable(){
public void run(){
String z=conList.getString(conList.getSelectedIndex());
rem.requestControl(z);
//rem.reqPic
}
};
new Thread(runner).start();
}
return;
}
}
void show(Displayable x){
Display.getDisplay(this).setCurrent(x);
}

public void addToList(String x){
devList.append(x,servImage);
}
public void appendToMainScreen(String x){
mainScreen.append(x);
}
public void addOperation(String x){
opList.append(x,opImage);
}
public void addControl(String x){
conList.append(x,opImage);
}
public void showInformation(String x){
Alert m=new Alert("Operation");
m.setType(AlertType.INFO);
m.setString(x);
m.setTimeout(1500);
show(m);
}
}
