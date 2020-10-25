package scanip;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.net.*;
import java.util.Date;

public class MyScanIP extends JFrame implements ActionListener{
	
	JLabel lb1,lb2,lb3,lb4;
	JTextField tf1,tf2,tf3,tf4;
	JTextArea ta;
	JScrollPane scroll;
	JButton scan,stop,exit;
	JPanel pn1,pn1a,pn1b,pn2,pn3,pn;
	
	InetAddress myip;
	NetworkInterface myni;
	short pl;
	
	byte[] ip= new byte[4];
	byte[] sm= new byte[4];
	byte[] na= new byte[4];
	byte[] ba= new byte[4];
	
	boolean stopscan;
	int scthread;
	
	public void GUI() {
		lb1=new JLabel("My IP Address:          ");
		lb2=new JLabel("Network Address:        ");
		lb3=new JLabel("Broadcast Address:      ");
		lb4=new JLabel("Scan Range:             ");
		
		tf1=new JTextField();tf1.setEditable(false);
		tf2=new JTextField();tf2.setEditable(false);
		tf3=new JTextField();tf3.setEditable(false);
		tf4=new JTextField();tf4.setEditable(false);
		
		tf1.setText(myip.getHostAddress()+" /"+Short.toString(pl));
		tf2.setText(stringip(decimalip(na)));
		tf3.setText(stringip(decimalip(ba)));
		tf4.setText(stringip(decimalip(na)+1)+" - "+stringip(decimalip(ba)-1));
		ta=new JTextArea();
		scroll = new JScrollPane(ta);
		
		scan=new JButton("Scan");
		stop=new JButton("Stop");stop.setEnabled(false);
		exit=new JButton("Exit");
		
		scan.addActionListener(this);
		stop.addActionListener(this);
		exit.addActionListener(this);
		
		pn1=new JPanel(new BorderLayout());
		pn1a=new JPanel(new GridLayout(4,1));
		pn1b=new JPanel(new GridLayout(4,1));
		pn2=new JPanel(new BorderLayout());
		pn3=new JPanel(new FlowLayout());
		pn=new JPanel(new GridLayout(2,1));
		
		pn1a.add(lb1);
		pn1b.add(tf1);
		pn1a.add(lb2);
		pn1b.add(tf2);
		pn1a.add(lb3);
		pn1b.add(tf3);
		pn1a.add(lb4);
		pn1b.add(tf4);
		pn1.add(pn1a,BorderLayout.WEST);
		pn1.add(pn1b,BorderLayout.CENTER);
	
		pn3.add(scan);
		pn3.add(stop);
		pn3.add(exit);
		
		pn2.add(scroll,BorderLayout.CENTER);
		pn2.add(pn3,BorderLayout.SOUTH);
		
		pn.add(pn1);
		pn.add(pn2);
		
		add(pn);
		setSize(450,400);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource()==scan) {
			scan.setEnabled(false);
			stop.setEnabled(true);
			ta.append("Start scanning...\n");
			for (scthread = 1; scthread <= 10; scthread++) {
				Thread scanThread = new Thread() {
					public void run() {
						scanning(scthread);
					}
				};
				scanThread.start();
			}
		}
		if(e.getSource()==stop) {
			stopscan=true;
		}
		if(e.getSource()==exit) {
			System.exit(0);
		}
	}
	
	byte[] subnetmask(short pl) {	//get subnet mask from prefix length
		byte[] octet = new byte[4];
		short t=pl;
		for (int i=0;i<4;i++) {
			if (t>=8) {
				octet[i]=(byte)255;
				t-=8;
			}
			else if (0<=t) {
				octet[i]=(byte)(256-Math.pow(2, 8-t));
				t=0;
			}
		}
		return octet;
	}
	byte[] networkaddress(byte[] ip,byte[] sm) {	//get network address from IP and subnet mask
		byte[] octet = new byte[4];
		for (int i=0;i<4;i++) {
			octet[i]=(byte) (ip[i]&sm[i]);
		}
		return octet;
	}
	byte[] broadcastaddress(byte[] na,byte[] sm) {	//get broadcast address from networkaddress and subnet mask
		byte[] octet = new byte[4];
		for (int i=0;i<4;i++) {
			octet[i]=(byte) (na[i]|(~sm[i]));
		}
		return octet;
	}
	long decimalip(byte[] ip){	//convert IP to a decimal value
		int oct[] = new int[4];
		long l=0;
		for (int i=0;i<4;i++) {
			oct[i]=ip[i]& 0xff;
			l+=oct[i]*Math.pow(256,3-i);
		}
		return l;
	}
	String stringip(long l) {	//get IP as a string from a decimal value
		String s="";
		long oct[] = new long[4];
		for (int i=3;i>=0;i--) {
			oct[i]=l%256;
			l/=256;
		}
		for (int i=0;i<4;i++) {
			s+=Long.toString(oct[i]);
			if(i<3) s+=".";
		}
		return s;
	}
	
	public void checkip() {
		try {
			myip= Inet4Address.getLocalHost();
			myni=NetworkInterface.getByInetAddress(myip);
			pl=myni.getInterfaceAddresses().get(0).getNetworkPrefixLength();
			
			ip=myip.getAddress();
			sm= subnetmask(pl);
			na= networkaddress(ip,sm);
			ba=broadcastaddress(na,sm);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void scanning(int scthread) {
		try {
			long start=decimalip(na);
			long end=decimalip(ba);
			for(long i=start+scthread;i<end;i+=10) {
				if (stopscan==true) break;
				String ipaddress= stringip(i); 
				InetAddress ia=InetAddress.getByName(ipaddress);
				boolean reached = ia.isReachable(3000);
			    if(reached) updateProgress(ipaddress);
			}
			finishScan();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	void updateProgress(String ipaddress) {
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	ta.append(ipaddress+"\n");
		    }
		  });
	}
	void finishScan() {
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	if(stopscan==true) {
		    		ta.append("Stopped!\n");
		    		stopscan=false;
		    	}
		    	else ta.append("Finished!\n");
		    	scan.setEnabled(true);
		    	stop.setEnabled(false);
		    	ta.append(new Date().toString());
		    }
		  });
	}
	
	public MyScanIP(String st) {
		super(st);
		checkip();
		GUI();
	}
	public static void main(String[] args) {
		new MyScanIP("My Scan IP");

	}

	
}
