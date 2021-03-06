/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package example.slavi.jaxb;
 
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import example.slavi.jaxb.bean.Items;
import example.slavi.jaxb.bean.PurchaseOrderType;
import example.slavi.jaxb.bean.USAddress;

/*
 * $Id$
 *
 * Copyright 2003 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
 
public class UnmarshalRead {
    
    // This sample application demonstrates how to unmarshal an instance
    // document into a Java content tree and access data contained within it.
    
    public static void main( String[] args ) {
        try {
            // create a JAXBContext capable of handling classes generated into
            // the primer.po package
            JAXBContext jc = JAXBContext.newInstance( "example.slavi.jaxb.bean" );
            
            // create an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();
            
            // unmarshal a po instance document into a tree of Java content
            // objects composed of classes from the primer.po package.
            JAXBElement<?> poElement = (JAXBElement<?>)u.unmarshal( UnmarshalRead.class.getResourceAsStream( "po.xml" ) );
            PurchaseOrderType po = (PurchaseOrderType)poElement.getValue();
            
            // examine some of the content in the PurchaseOrder
            System.out.println( "Ship the following items to: " );
            
            // display the shipping address
            USAddress address = po.getShipTo();
            displayAddress( address );
            
            // display the items
            Items items = po.getItems();
            displayItems( items );
            
        } catch( JAXBException je ) {
            je.printStackTrace();
        }
    }
    
    public static void displayAddress( USAddress address ) {
        // display the address
        System.out.println( "\t" + address.getName() );
	name = address.getName();
        System.out.println( "\t" + address.getStreet() ); 
        System.out.println( "\t" + address.getCity() +
                            ", " + address.getState() + 
                            " "  + address.getZip() ); 
        System.out.println( "\t" + address.getCountry() + "\n"); 
    }
    
    public static void displayItems( Items items ) {
        // the items object contains a List of primer.po.ItemType objects
        List itemTypeList = items.getItem();

                
        // iterate over List
        for( Iterator iter = itemTypeList.iterator(); iter.hasNext(); ) {
            Items.Item item = (Items.Item)iter.next(); 
            System.out.println( "\t" + item.getQuantity() +
                                " copies of \"" + item.getProductName() +
                                "\"" ); 
        }
    }

    public static String getName() {
	if (name == null) {
		main(null);
	}
	return name;
    }
   
    private static String name;
}