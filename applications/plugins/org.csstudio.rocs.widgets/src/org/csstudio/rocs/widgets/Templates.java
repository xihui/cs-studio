package org.csstudio.rocs.widgets;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;





import javax.xml.bind.*;


public class Templates {

	private Templates(){
		
	}
	
	private static ExecutorService defaultExecutor = Executors.newSingleThreadExecutor(Templates.namedPool("ROCS Template"));
	
	
	public static Template createFromXml(String name, InputStream input){
	try{
		
		JAXBContext jc = JAXBContext.newInstance(XMLDisplay.class, XMLWidget.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        
        XMLDisplay payload = (XMLDisplay) unmarshaller.unmarshal(input);

        TemplateDescription templateDescription = new TemplateDescription(name);
        templateDescription.executorService = defaultExecutor;
       
        
        for(XMLWidget widget : payload.widgets) {
            //System.out.println(o.getClass());
        	templateDescription.addWidget(widget);
        }
        
		
			return new Template(templateDescription);
    	} catch (Exception ex) {
    		Logger.getLogger(Templates.class.getName()).log(Level.FINEST, "Couldn't create template", ex);
    		throw new IllegalArgumentException("Couldn't create template", ex);
    	}
	}
	
	public static ThreadFactory namedPool(String poolName) {
        return new DefaultThreadFactory(poolName);
    }
    
    /**
     * Taken from {@link Executors#defaultThreadFactory() }.
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() :
                                 Thread.currentThread().getThreadGroup();
            namePrefix = poolName;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (!t.isDaemon())
                t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
