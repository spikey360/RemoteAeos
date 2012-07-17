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
private final Command gotoDevList=new Command("Devices",Command.ITEM,2);
private final Command exitCommand=new Command("Exit",Command.EXIT,2);
private final Command startSearch=new Command("Search", Command.OK,1);
private final Command gotoOpList=new Command("Operations",Command.ITEM,3);
private final Command preview=new Command("Preview",Command.ITEM,4);
private final Command nextOp=new Command("Next",Command.ITEM,1);
private final Command showOpInfo=new Command("Info",Command.ITEM,2);
private final Form mainScreen=new Form("AeosRemote");
private final String[] elem=new String[]{};
private RemoteData rem;
//list of devices found
private final List devList=new List("AeosPy servers",List.IMPLICIT,elem,null);
//list of operations available
private final List opList=new List("Operations",List.IMPLICIT,elem,null);
public AeosRemote(){
mainScreen.addCommand(startSearch);
mainScreen.addCommand(exitCommand);
mainScreen.addCommand(gotoDevList);
mainScreen.addCommand(gotoOpList);
mainScreen.setCommandListener(this);
devList.addCommand(mainScreenBack);
devList.setCommandListener(this);
opList.addCommand(mainScreenFromOpList);
opList.addCommand(showOpInfo);
//opList.addCommand(preview);
opList.addCommand(nextOp);
opList.setCommandListener(this);
}
public void startApp(){
show(mainScreen);
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
if(d==devList){
 show(mainScreen);
 if(c!=mainScreenBack){
 Runnable runner=new Runnable(){
 public void run(){
  rem.establishConnection(devList.getSelectedIndex());
  rem.readProtocolString();
  }
 };
 new Thread(runner).start();
 }
 return;
 /*switch(devList.getSelectedIndex()){
 case 0:
  //search option
  break;
 default:
  System.err.println("Woah");
  break;
 }*/
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
}
void show(Displayable x){
Display.getDisplay(this).setCurrent(x);
}

public void addToList(String x){
devList.append(x,null);
}
public void appendToMainScreen(String x){
mainScreen.append(x);
}
public void addOperation(String x){
opList.append(x,null);
}
public void showInformation(String x){
Alert m=new Alert("Operation");
m.setType(AlertType.INFO);
m.setString(x);
m.setTimeout(1500);
show(m);
}
}
