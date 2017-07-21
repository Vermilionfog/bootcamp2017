package com.example.android.camera2basic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XMLReader {
    public void domRead(String file) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);

        Element root = document.getDocumentElement();

        //ルート要素のノード名を取得する
        System.out.println("ノード名：" +root.getNodeName());

        //ルート要素の属性を取得する
        System.out.println("ルート要素の属性：" + root.getAttribute("name"));

        //ルート要素の子ノードを取得する
        NodeList rootChildren = root.getChildNodes();

        System.out.println("子要素の数：" + rootChildren.getLength());
        System.out.println("------------------");

        for(int i=0; i < rootChildren.getLength(); i++) {
            Node node = rootChildren.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                if (element.getNodeName().equals("person")) {
                    System.out.println("名前：" + element.getAttribute("name"));
                    NodeList personChildren = node.getChildNodes();

                    for (int j=0; j < personChildren.getLength(); j++) {
                        Node personNode = personChildren.item(j);
                        if (personNode.getNodeType() == Node.ELEMENT_NODE) {

                            if (personNode.getNodeName().equals("age")) {
                                System.out.println("年齢：" + personNode.getTextContent());
                            } else if (personNode.getNodeName().equals("interest")) {
                                System.out.println("趣味:" + personNode.getTextContent());
                            }

                        }
                    }
                    System.out.println("------------------");
                }
            }


        }


    }
}