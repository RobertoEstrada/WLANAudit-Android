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

public class ProjectGenerator {
    /**
     * Project name
     */
    private String mProjectName;
    /**
     * Directory where to put the .project file
     */
    private File mOutputPath;
    
    public ProjectGenerator(String projectName, File outputPath) {
        mProjectName = projectName;
        mOutputPath = outputPath;
    }
    
    public void generateProject() {
        File projFile = new File(mOutputPath, ".project");
        try {            
            String contents = String
                    .format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><projectDescription><name>%s</name><comment></comment><projects></projects><buildSpec><buildCommand><name>com.android.ide.eclipse.adt.ResourceManagerBuilder</name><arguments></arguments></buildCommand><buildCommand><name>com.android.ide.eclipse.adt.PreCompilerBuilder</name><arguments></arguments></buildCommand><buildCommand><name>org.eclipse.jdt.core.javabuilder</name><arguments></arguments></buildCommand><buildCommand><name>com.android.ide.eclipse.adt.ApkBuilder</name><arguments></arguments></buildCommand></buildSpec><natures><nature>com.android.ide.eclipse.adt.AndroidNature</nature><nature>org.eclipse.jdt.core.javanature</nature></natures></projectDescription>",
                            mProjectName);
            
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(contents));
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
            FileWriter outputWriter = new FileWriter(projFile);
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
