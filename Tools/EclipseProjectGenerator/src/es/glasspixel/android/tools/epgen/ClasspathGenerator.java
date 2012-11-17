/*
 * Copyright (C) 2012 Roberto Estrada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.glasspixel.android.tools.epgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ClasspathGenerator {
    /**
     * Directory where to put the .project file
     */
    private File mProjectPath;
    
    public ClasspathGenerator(File projectPath) {
        mProjectPath = projectPath;
    }
    
    public void generateProject() {
        File classpathFile = new File(mProjectPath, ".classpath");
        try {            
            StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            sb.append("<classpath>");
            sb.append("<classpathentry kind=\"con\" path=\"com.android.ide.eclipse.adt.ANDROID_FRAMEWORK\"/>");
            sb.append("<classpathentry kind=\"con\" path=\"com.android.ide.eclipse.adt.LIBRARIES\"/>");
            sb.append("<classpathentry kind=\"src\" path=\"src\"/>");
            sb.append("<classpathentry kind=\"src\" path=\"gen\"/>");
            sb.append("<classpathentry kind=\"output\" path=\"bin/classes\"/>");
            
            File libDir = new File(mProjectPath, "libs");
            
            if (libDir.exists()) {
                File[] libs = libDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".jar");
                    }
                });

                for (File lib : libs) {
                    sb.append(String.format("<classpathentry kind=\"lib\" path=\"libs/%s\"/>",
                            lib.getName()));
                }
            }
            
            sb.append("</classpath>");
                        
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(sb.toString()));
            Document doc = db.parse(is);
            
            // create a transformer
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer        transformer  = transFactory.newTransformer();
            
            // get a transformer and supporting classes
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            DOMSource    source = new DOMSource(doc);
           
            // transform the xml document into a string
            transformer.transform(source, result);
           
            // open the output file
            FileWriter outputWriter = new FileWriter(classpathFile);
            outputWriter.write(writer.toString());
            outputWriter.close();
            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
