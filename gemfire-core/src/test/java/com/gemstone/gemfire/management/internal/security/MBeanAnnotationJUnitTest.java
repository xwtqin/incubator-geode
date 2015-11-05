package com.gemstone.gemfire.management.internal.security;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.GemFireConfigException;
import com.gemstone.gemfire.management.internal.cli.util.ClasspathScanLoadHelper;
import com.gemstone.gemfire.management.internal.security.JMXOperationContext;
import com.gemstone.gemfire.management.internal.security.ResourceOperation;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

import junit.framework.TestCase;

/**
 * This test will ensure that all the MBean operations are properly annotated or not.
 * @author rishim
 *
 */
@Category(UnitTest.class)
public class MBeanAnnotationJUnitTest extends TestCase {

	public MBeanAnnotationJUnitTest(String name) {
		super(name);
	}
	
	public static List<String> excludedClasses = new ArrayList<String>();

	public void setUp() throws Exception {
     excludedClasses.add("com.gemstone.gemfire.management.CustomMXBean");
	}

	public void testMBeanAnnotation() {
		List<String> notFoundList = new ArrayList<String>();
		try {
			Class[] klassList = ClasspathScanLoadHelper
					.getClasses("com.gemstone.gemfire.management");
			for (Class klass : klassList) {
				if (klass.getName().endsWith("MXBean")) {
				  if(excludedClasses.contains(klass.getName())){
				    continue;
				  }
					Method[] methods = klass.getMethods();
					for (Method method : methods) {
						String name = method.getName();
						if(JMXOperationContext.isGetterSetter(name)){
							continue;
						}
						
						boolean found = false;
						Annotation ans[] = method.getDeclaredAnnotations();
						for (Annotation an : ans) {
							if (an instanceof ResourceOperation) {
								found = true;
								break;
							}
						}
						if (!found) {
							notFoundList.add(klass + ":" + name);
						}
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new GemFireConfigException(
					"Error while testing annotation for jmx - ", e);
		} catch (IOException e) {
			throw new GemFireConfigException(
					"Error while testing annotation for jmx - ", e);
		}
		
		for (String s : notFoundList) {
			System.out.println(s);
		}

		assertTrue(notFoundList.size() == 0);
	}

}
