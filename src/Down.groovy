import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @author dnoseda@gmail.com
 *
 */

String URL_BASE_TEMPLATE = "http://submanga.com/\${name}/completa";
String CMD_GET_TEMPLATE = "sh ~/bin/downc.sh \${name} \${base}\n";

def arguments = [:]
args.each { arg ->
	String[] aux = arg.split("=");
	arguments[aux[0]] = aux[1]
}

String urlBase = StrSubstitutor.replace(URL_BASE_TEMPLATE,ImmutableMap.of("name",arguments.get("name").replaceAll(" ", "_")));
String beginWith= arguments.name + " ";
String outputFile =arguments.name.replaceAll(" ", "_")+".sh";


WebDriver driver = new FirefoxDriver();
driver.get(urlBase);
def caps = [:]
StrBuilder str = new StrBuilder();
File file = new File(outputFile);
if(!file.exists()){
	FileUtils.touch(file);
}
for(WebElement element:driver.findElements(By.partialLinkText(beginWith))){
	if(element.getText() ==~ "${beginWith} \\d+"){
		caps.put(element.getText(),element.getAttribute("href"));
		println("entro: ${element.getText()}");
	}
}
caps.each{ key, cap ->
	String name = key.replaceAll(" ", "");
	String base = "";
	driver.get(cap);
	for(WebElement link: driver.findElements(By.xpath("//div"))){
		String attr = link.getAttribute("style");
		if(attr!=null && attr.startsWith("background")){
			println ("encontro '${attr}'");
			base = StringUtils.substringBetween(attr,"url(\"", "1.jpg");
			println ("queda como '${base}'");
		}
	}
	str.append(StrSubstitutor.replace(CMD_GET_TEMPLATE, ImmutableMap.of("name",name,"base",base)));
	print(".");
	file.append(str.toString());
	str = new StrBuilder();
}
file.append(str.toString());
driver.close();

