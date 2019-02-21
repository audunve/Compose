package utilities;

import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;

public class XSDParser {
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
		
		System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance(); 
		com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl impl = (XSImplementationImpl) registry.getDOMImplementation("XS-Loader");
		XSLoader schemaLoader = (XSLoader) impl.createXSLoader(null);
		XSModel model = schemaLoader.loadURI("src/test/resources/my.xsd");
		
		
	}

}
