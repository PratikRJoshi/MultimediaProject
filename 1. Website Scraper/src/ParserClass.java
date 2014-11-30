import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ParserClass {
	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String args[]) throws IOException{
		
		//home page
		String baseURL ="http://www.wmuseumaa.org/index.cfm";
		Document homeDoc = Jsoup.connect(baseURL).get();
//		System.out.println(homeDoc.html());
		
		//click on collections option in the tab
		Elements collectionHref = homeDoc.select("div");
//		System.out.println(homeDoc.select("div").get(7).select("a").attr("href"));
		String collectionURL = homeDoc.select("div").get(7).select("a").attr("href");
		Document collectionDoc = Jsoup.connect(collectionURL).get();
//		System.out.println(collectionDoc.html());
		
		//click on the browse the collections option on the right
		String browsePartURL = (collectionDoc.getElementsByClass("main").get(12).select("tr").get(3).select("td").get(1).select("a").attr("href"));
		String browseURL = collectionURL + browsePartURL;
		Document browseCollectionDoc = Jsoup.connect(browseURL).timeout(10000*10000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
//		System.out.println(browseCollectionDoc.html());
		
		//get the URL for permanent collection
		String permanentPartURL = (browseCollectionDoc.getElementsByClass("title").first().select("a").attr("href"));
		String permanentURL = collectionURL + permanentPartURL;
		Document permanentCollectionDoc = Jsoup.connect(permanentURL).timeout(10000*10000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
//		System.out.println(permanentCollectionDoc.html());
		
		//get the URL for Paintings on the page
		String paintingPartURL = (permanentCollectionDoc.getElementsByClass("title").get(2).select("a").attr("href"));
		String paintingURL = collectionURL+paintingPartURL;
		Document paintingsCollectionDoc = Jsoup.connect(paintingURL).timeout(10000*10000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
//		System.out.println(paintingsCollectionDoc.html());
		
		
		String paintingPageURL[] = new String[30];
		for(int i=1,k=1;i<600;i+=12,k++){					//storing the URLs for all the paintings pages..this is how the pagination is handled
			if(k==31)
				break;
			paintingPageURL[k-1] = "http://collection.wmuseumaa.org/PRT4?rec="+i+"&sid=259&x=10037&port=4";
//			System.out.println(k+": "+paintingPageURL[k-1]);
		}

		
		int paintingCount=1;
		File jFile = new File("PaintingRecords.json");	
		FileWriter fw = new FileWriter(jFile);
		JSONArray jArray = new JSONArray();				//JSON array to store JSON objects
		
		for(int i=0;i<paintingPageURL.length;i++){
			Document paintingsPageDoc = Jsoup.connect(paintingPageURL[i]).timeout(10000*10000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
//			System.out.println(paintingsIndividualDoc.html());
			for(int j=1,k=2;j<=12;j++,k+=4, paintingCount++){
				String urls = paintingsPageDoc.getElementsByClass("main").get(10).select("td").get(k).select("a").attr("href");
				String paintingIndividualURL = collectionURL + urls;	//
//				System.out.println(paintingCount+" \nPainting URL: "+paintingIndividualURL);
				Document paintingsIndividualDoc = Jsoup.connect(paintingIndividualURL).timeout(10000*10000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
				String imgSrc = collectionURL+paintingsIndividualDoc.getElementsByClass("main").get(10).select("td").get(0).select("img").attr("src");	//
//				System.out.println("Image src = "+collectionURL+paintingsIndividualDoc.getElementsByClass("main").get(10).select("td").get(0).select("img").attr("src"));
//				System.out.println("Data:\n"+paintingsIndividualDoc.getElementsByClass("main").get(10).select("td").get(2));
				String paintingTitle = (paintingsIndividualDoc.getElementsByClass("titlered").text());
				Element el = paintingsIndividualDoc.getElementsByClass("main").get(10).select("td").get(2);
//				System.out.println(el.select("a"));
				String artistName = el.select("a").text();	//
				String artistHref = collectionURL + el.select("a").attr("href");	
				String textOnly = Jsoup.parse(el.html()).text();
				System.out.println(paintingCount+": "+textOnly);
				
				//splitting parameters
				String credit = "Credit Line";
				String primaryAccession = "Primary Accession #";
				String mediumAndSupport = "Medium and Support";
				String lifePlace = "PlaceAndDay";
				String year = "Year";
				String dim = "Dimensions";
				String artInfo = "Artist Information";
				String dims = "Dimensions";
				
				//primary accession number
				String accessionNo = ((textOnly.split(primaryAccession))[1].split("Credit Line")[0]);
				
				//credit line
				//handle null credit lines
				String creditLine="";
				if(textOnly.contains("Credit Line :"))
					creditLine = ((textOnly.split(credit))[1].split(mediumAndSupport)[0]);
				
				//medium and suport
				String mAndS = "";
				if(textOnly.contains(mediumAndSupport))
					mAndS = (textOnly.split(mediumAndSupport)[1]);
						
				
				//check if dimensions is present; if yes, then use it to get the year and the dimension value
				//else use the artist information to split
				String yearOfPublish="", dimensions="";
				
				//handle the presence of () in the painting title
				String addP = paintingTitle.replaceAll("\\)", "\\\\)");
				addP = addP.replaceAll("\\(", "\\\\(");
//				System.out.println(addP.replace("\\", "\\\\"));
				
				if(textOnly.contains(dims)){
					yearOfPublish = textOnly.split(addP)[1].split(dim)[0];
					dimensions = textOnly.split(dim)[1].split(artInfo)[0];
				}
				else{
					yearOfPublish = textOnly.split(artInfo)[0].split(addP)[1];
					dimensions = "";
				}
				
				//date and place of birth and death
				//handle the presence of () in the names
				String add = artistName.replaceAll("\\)", "\\\\)");
				add = add.replaceAll("\\(", "\\\\(");
//				System.out.println(add.replace("\\", "\\\\"));
				String dPoBD="";
				if(!artistName.contains("Unknown ")&&!artistName.contains("William Rice")&&!artistName.contains("Eakins"))
					dPoBD = textOnly.split(primaryAccession)[0].split(add)[1];
				if(artistName.contains("William Rice"))
					dPoBD = "(1777 - 1847)";
				if(artistName.contains("Eakins"))
					dPoBD = "(1852 - 1938)";
				
				//handle the : in the values of mAndS and accessionNo
				mAndS = mAndS.replaceAll(":"," ");
				accessionNo = accessionNo.replaceAll(":"," ");
				
				
				JSONObject jObj = new JSONObject();
				jObj.put("Painting Title", paintingTitle);
				jObj.put("Painting URL", paintingIndividualURL);
				jObj.put("Image URL", imgSrc);
				jObj.put("Artist Name", artistName);
				jObj.put("Artist data URL", artistHref);
				jObj.put(year, yearOfPublish);
				jObj.put(dims, dimensions);
				jObj.put(lifePlace, dPoBD);
				jObj.put(credit, creditLine);
				jObj.put(primaryAccession, accessionNo);
				jObj.put(mediumAndSupport, mAndS);
				
				jArray.add(jObj);
				
				
				
				
					
			}
		}
		fw.write(jArray.toJSONString());
		fw.write("\n");
		fw.flush();
		fw.close();
	}
}

