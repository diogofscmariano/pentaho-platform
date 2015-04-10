package org.pentaho.wadl;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.jersey.wadl.resourcedoc.ResourceDoclet;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ResourceDocType;
import com.sun.jersey.wadl.resourcedoc.DocProcessorWrapper;

public class PentahoResourceDoclet extends ResourceDoclet {
  private static final String OUTPUT_FILE_NAME = "wadlExtension.xml";
  private static final String OUTPUT_PATH_PARAM = "-output";

  private static boolean isDeprecated( AnnotationDesc[] annotationDescs ) {
    for ( AnnotationDesc annotationDesc : annotationDescs ) {
      if ( "deprecated".equalsIgnoreCase( annotationDesc.annotationType().name() ) ) {
        return true;
      }
    }
    return false;
  }

  private static boolean isPublic( String documentation ) {
    return documentation != null && !"".equals( documentation.trim() );
  }

  private static String getVisibility( boolean isPublic ) {
    if ( isPublic ) {
      return "Public";
    }
    return "Private";
  }

  private static String generateComment( MethodDoc methodDoc ) {
    boolean isDeprecated = isDeprecated( methodDoc.annotations() );
    String documentation = methodDoc.commentText();
    boolean isPublic = isPublic( documentation );

    StringBuilder comment = new StringBuilder();
    comment.append( "<visibility>" + getVisibility( isPublic ) + "</visibility>" );
    if ( isDeprecated ) {
      comment.append( "<deprecated>true</deprecated>" );
    }

    if ( isPublic ) {
      comment.append( "<documentation>" + documentation + "</documentation>" );
    }

    return comment.toString();
  }

  public static boolean start( RootDoc root ) {
    final String outputPath = getOutputPath( root.options() );
    final DocProcessorWrapper docProcessor = new DocProcessorWrapper();

    final ResourceDocType result = new ResourceDocType();
    final ClassDoc[] classes = root.classes();

    for ( ClassDoc classDoc : classes ) {
      final ClassDocType classDocType = new ClassDocType();
      classDocType.setClassName( classDoc.qualifiedTypeName() );
      classDocType.setCommentText( classDoc.commentText() );
      docProcessor.processClassDoc( classDoc, classDocType );

      for ( MethodDoc methodDoc : classDoc.methods() ) {
        final MethodDocType methodDocType = new MethodDocType();

        methodDocType.setMethodName( methodDoc.name() );
        methodDocType.setCommentText( generateComment( methodDoc ) );

        docProcessor.processMethodDoc( methodDoc, methodDocType );
        classDocType.getMethodDocs().add( methodDocType );
      }
      result.getDocs().add( classDocType );
    }

    try {
      final Class<?>[] clazzes = { result.getClass() };
      final JAXBContext c = JAXBContext.newInstance( clazzes );
      final Marshaller m = c.createMarshaller();
      m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
      final OutputStream out = new BufferedOutputStream( new FileOutputStream( outputPath ) );
      final XMLSerializer serializer = getXMLSerializer( out );
      m.marshal( result, serializer );
      out.close();
    } catch ( Exception e ) {
      return false;
    }

    return true;
  }

  private static XMLSerializer getXMLSerializer( OutputStream os ) throws InstantiationException,
      IllegalAccessException, ClassNotFoundException {
    OutputFormat of = new OutputFormat();
    of.setCDataElements( new String[] { "ns1^commentText", "ns2^commentText", "^commentText" } );
    XMLSerializer serializer = new XMLSerializer( of );
    serializer.setOutputByteStream( os );
    return serializer;
  }

  private static String getOutputPath( String[][] optionsMap ) {
    if ( optionsMap != null ) {
      for ( int i = 0; i < optionsMap.length; i++ ) {
        String[] option = optionsMap[i];

        if ( option[0].equals( OUTPUT_PATH_PARAM ) ) {
          return option[1];
        }
      }
    }

    return OUTPUT_FILE_NAME;
  }

}
