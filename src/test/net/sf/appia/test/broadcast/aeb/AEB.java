/**
 * Appia: Group communication and protocol composition framework library
 * Copyright 2006 University of Lisbon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * Initial developer(s): Alexandre Pinto and Hugo Miranda.
 * Contributor(s): See Appia web page for a list of contributors.
 */
/*
 * Created on Mar 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.sf.appia.test.broadcast.aeb;

import java.io.File;
import java.io.IOException;
import net.sf.appia.core.Appia;
import net.sf.appia.xml.AppiaXML;

import org.xml.sax.SAXException;



/**
 * @author jmocito
 */
public class AEB {


    private AEB() {}


    public static void main(String[] args) {

        /*
         * Example:    java AEB 1.xml
         * 
         * */
        		if (args.length == 1) {
        			final File xmlfile = new File(args[0]);
        			
        			try {
        				AppiaXML.load(xmlfile);
        				Appia.run();
        			} catch (SAXException e) {
        				e.printStackTrace();
        			} catch (IOException e) {
        				e.printStackTrace();
        			}
        		}


        else {
            System.out.println("Invalid number of arguments!");
            System.out.println("\tjava AEB <xml_file>");
        }
    }
}
