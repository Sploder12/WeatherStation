package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


import sploder12.json.JSON;

public class WebPeel extends Canvas implements Runnable{
	private static final long serialVersionUID = -2774293097493669472L;
	public static JSON json = new JSON();
	public static URL url;
	public static String webhtml;
	public static Thread render;
	public static int WIDTH = 1280, HEIGHT = 900;
	public static Font newFont, currentFont;
	public static Graphics2D g;
	
	public static float[] tempgraph = new float[192];
	public static float[] humgraph = new float[192];
	public final int interval = 450000; //450000
	public long time = System.currentTimeMillis() - interval;
	public WebPeel() {
		new Window(WIDTH, HEIGHT, "Tastes like the weather.", this);
		//float[] vals = {12.0f, 0.5f};
	   while(true) {
		   if(System.currentTimeMillis() - time >= interval) {
			   time += interval;
			   addValues(getWeather());
		   }
		   try {
			   Thread.sleep(1);
		   }catch(Exception e) {
			   e.printStackTrace();
		   }
	   }
	   //System.out.println(weather[0] + " Fahrenheit");
	   //System.out.println((weather[1]*100)+"%");
	}
	
	public void addValues(float[] temphum) {
		shift();
		tempgraph[tempgraph.length-1] = temphum[0];
		humgraph[humgraph.length-1] = temphum[1]*100;
	}
	
	private void shift() {
		float[] temptempgraph = tempgraph;
		float[] temphumgraph = humgraph;
		for(short i = 0; i < tempgraph.length-1; i++) {
			tempgraph[i] = temptempgraph[i+1];
			humgraph[i] = temphumgraph[i+1];
		}
		
	}
	
	public synchronized void start(){
		render = new Thread(this);
		render.start();
	}
	
	public void run() {
		while(true) {
			render();
		}
	}
	
	
	public synchronized void stop(){
		try{
			render.join();   
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null){
			this.createBufferStrategy(2);          //Makes the FPS not 31mil also prevents flashing
			return;
		}
		g =  (Graphics2D) bs.getDrawGraphics(); 
		 
		currentFont = g.getFont();
		newFont = currentFont.deriveFont((currentFont.getSize() * 1.8F)); 
		g.setFont(newFont);
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		  
		g.setColor(Color.DARK_GRAY);
		for(short i = 0; i < 10; i++) {
			g.drawLine(100,100+50*i,1060, 100+50*i);
		}
		for(short i = 1; i <= 96; i++) {
			g.drawLine(100+10*i, 100,100+10*i , 600);
		}
		g.setColor(Color.GRAY);
		for(short i = 1; i <= 24; i++) {
			g.drawLine(100+40*i, 100, 100+40*i, 600);
		}
		
		g.drawString("Time Until Next Data:"+(((interval-(System.currentTimeMillis()-time))/1000)+1)+" Seconds", 910, 830);
		
		g.setColor(Color.white);
	    g.drawLine(100, 100, 100, 600);
	    g.drawLine(100, 600, 1060, 600);
		g.drawString("100", 50, 100);
		g.drawString("50", 65, 360);
		g.drawString("0",75,600);
		g.drawString("24",90,630);
		g.drawString("Now",1040,630);
		g.drawString("12", 568, 630);
		g.drawString("18", 325, 630);
		g.drawString("6", 815, 630);
		g.drawString("3", 935, 630);
		g.drawString("9", 695, 630);
		g.drawString("15", 445, 630);
		g.drawString("21", 210, 630);
		
		g.drawString("Time Since Launch", 500, 670);
		g.drawString("(Hours)", 550, 700);
		
	
		for(short drawGraph = 0; drawGraph < tempgraph.length; drawGraph++) {
			g.setColor(Color.red);
			if(tempgraph[drawGraph] != 0.0f && drawGraph != humgraph.length-1) {
				g.drawLine(5*drawGraph+100,Math.round((5*(100-tempgraph[drawGraph])+100)),5*drawGraph+105,Math.round(5*(100-tempgraph[drawGraph+1])+100));
			}else if(drawGraph == humgraph.length-1) {
				g.drawLine(5*drawGraph+100,Math.round((5*(100-tempgraph[drawGraph])+100)),5*drawGraph+105,Math.round(5*(100-tempgraph[drawGraph])+100));
			}
		}
		g.drawString("Current Temp: "+tempgraph[tempgraph.length-1]+"°F",25,780);
		for(short drawGraph = 0; drawGraph < humgraph.length; drawGraph++) {
			g.setColor(Color.blue);
			if(humgraph[drawGraph] != 0.0f && drawGraph != humgraph.length-1) {
				g.drawLine(5*drawGraph+100,Math.round((5*(100-humgraph[drawGraph])+100)),5*drawGraph+105,Math.round(5*(100-humgraph[drawGraph+1])+100));
			}else if(drawGraph == humgraph.length-1) {
				g.drawLine(5*drawGraph+100,Math.round((5*(100-humgraph[drawGraph])+100)),5*drawGraph+105,Math.round(5*(100-humgraph[drawGraph])+100));
			}
		}
		g.drawString("Current Humidity: "+humgraph[humgraph.length-1]+'%', 25, 820);
		
		g.dispose(); 
	    bs.show();
	    try {
	    	Thread.sleep(1000);
	    }catch(Exception e) {
	    	e.printStackTrace();
	    }
	}
	
	private float[] getWeather() {
		float[] out = new float[2];
		webhtml = getHTML("https://api.darksky.net/forecast/5624aa715652c24b16c454c7439f0596/42.9751,-78.5929");
		out[0] = json.getFloatValueOfDict(webhtml, json.locateStringEnd(webhtml, "temperature"));
		out[1] = json.getFloatValueOfDict(webhtml, json.locateStringEnd(webhtml, "humidity"));
		return out;
	}
	
public String[]  getByString(String html, String string, int length) {
		int[] indexs = json.findAllIndexOfString(html, string, length); //gets the locations of the strings
		int len = indexs.length;
		int stringlen = string.length();
		String[] strings = new String[len];
		for(int x = 0; x < len; x++) {
				strings[x] = html.substring(indexs[x]-stringlen,indexs[x]+stringlen+1); //removes irrelavent data using substring
		}
		return strings;
	}

public String[] getByString(String html, String string) {
		return getByString(html, string, 100);
	}
private void getGarys() {//Gets Data From Gary Pools Website
	webhtml = getHTML("http://www.garypools.net/");
	   String[] prices =  getByClass("price price--withoutTax",webhtml);
	   int pricelen = prices.length;
	   String[] items = getByClass("card-title", webhtml, 24);
	   int itemslen = items.length;
	   for(int x = 0; x < itemslen; x++) {
		   items[x] = items[x].substring(json.locateStringEnd(items[x], ">"));
		   items[x] = getapos(items[x]);
	   }
	   System.out.println(itemslen +"   "+pricelen);
	   Float[] price = new Float[pricelen];
	   for(int x = 0; x < pricelen; x++) {
		   if(x < itemslen) {
			   System.out.println(items[x]+":"+prices[x*2]);
		   }
		   prices[x] = prices[x].substring(1);
		   price[x] = Float.parseFloat(prices[x]);
	   }
}

public String getHTML(String url) {
	InputStream is = null;
    BufferedReader br;
    String line, page ="";
    try {
        WebPeel.url = new URL(url);
        is = WebPeel.url.openStream();  // throws an IOException
        br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            page+= line;
        }
    } catch (MalformedURLException mue) {
         mue.printStackTrace();
    } catch (IOException ioe) {
         ioe.printStackTrace();
    } finally {
        try {
            if (is != null) is.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    return page;
}

public String getapos(String string) {
	String outstring =string;
	int start = json.locateStringStart(string, "&#x27;");
	if(start != -1) {
		outstring = string.substring(0, start);
		outstring += "'";
		outstring += string.substring(start+6);
		
	}
	return outstring;
}
private String[] getByFrame(String thing, int[] locations, String webhtml) {
	int amount = locations.length; 
	int[] endlocations = new int[amount];
	String[] output = new String[amount];
	for(int get = 0; get < amount; get++) {
		locations[get] = json.locateStringEnd(webhtml, ">", locations[get]); //finds all the ">" in the html
		endlocations[get] = json.locateStringStart(webhtml, "</", locations[get]); //finds all the "</" in the html
		output[get] = webhtml.substring(locations[get], endlocations[get]); //uses substring to remove tags
	}
	return output;
}
public String[] getByMiscAttribute(String misc, String webhtml, int total) {
	 int[] indexs = json.findAllIndexOfString(webhtml, misc, total);
	return getByFrame(misc, indexs , webhtml);
}
public String[] getByMiscAttribute(String misc, String webhtml) {
	return getByMiscAttribute(misc,webhtml,100);
}
public String[] getByTag(String tag, String webhtml, int tagtotal) { //I dont recommend using tag
	int[] locations = json.findAllIndexOfString(webhtml, '<'+tag, tagtotal+1); //note that the < is automatically added
	return getByFrame(tag,locations, webhtml);
}
public String[] getByTag(String tag, String webhtml) {
	return getByTag(tag, webhtml, 100);
}
public String[] getById(String id, String webhtml, int tagtotal) {
	int[] locations = json.findAllIndexOfString(webhtml, "id="+'"'+id+'"', tagtotal+1);
	return getByFrame(id,locations, webhtml);
}
public String[] getById(String id, String webhtml) {
	return getById(id, webhtml, 100);
}
public String[] getByClass(String Class, String webhtml, int tagtotal) {
	int[] locations = json.findAllIndexOfString(webhtml, "class="+'"'+Class+'"', tagtotal+1);
	return getByFrame(Class, locations, webhtml);
}
public String[] getByClass(String Class, String webhtml) {
	return getByClass(Class, webhtml,100);
}
	public static void main(String[] args) {
		
		new WebPeel();

	}
	
}
