package org.pentaho.wadl.helpers;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;

public class DefaultAnnotationDesc implements AnnotationDesc {

  @Override
  public AnnotationTypeDoc annotationType() {
    AnnotationTypeDoc annotationTypeDoc = new DefaultAnnotationTypeDoc();
    return annotationTypeDoc;
  }

  @Override
  public ElementValuePair[] elementValues() {
    return null;
  }

}
