/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.commons.hibernate.util;

import java.sql.Connection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.util.ConfigHelper;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.base._BaseRootDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Tomas Muller
 */
public class HibernateUtil {
    private static Log sLog = LogFactory.getLog(HibernateUtil.class);
    private static SessionFactory sSessionFactory = null;

	private static void setProperty(org.w3c.dom.Document document, String name, String value) {
        if (value==null) {
            removeProperty(document, name);
        } else {
            org.w3c.dom.Element hibConfiguration = (org.w3c.dom.Element)document.getElementsByTagName("hibernate-configuration").item(0);
            org.w3c.dom.Element sessionFactoryConfig = (org.w3c.dom.Element)hibConfiguration.getElementsByTagName("session-factory").item(0);
            NodeList properties = sessionFactoryConfig.getElementsByTagName("property");
            for (int i=0;i<properties.getLength();i++) {
                org.w3c.dom.Element property = (org.w3c.dom.Element)properties.item(i);
                if (name.equals(property.getAttribute("name"))) {
                    Text text = (Text)property.getFirstChild();
                    if (text==null) {
                        property.appendChild(document.createTextNode(value));
                    } else {
                        text.setData(value);
                    }
                    return;
                }
            }
            org.w3c.dom.Element property = document.createElement("property");
            property.setAttribute("name",name);
            property.appendChild(document.createTextNode(value));
            sessionFactoryConfig.appendChild(property);
        }
	}
	
	private static void removeProperty(org.w3c.dom.Document document, String name) {
		org.w3c.dom.Element hibConfiguration = (org.w3c.dom.Element)document.getElementsByTagName("hibernate-configuration").item(0);
		org.w3c.dom.Element sessionFactoryConfig = (org.w3c.dom.Element)hibConfiguration.getElementsByTagName("session-factory").item(0);
		org.w3c.dom.NodeList properties = sessionFactoryConfig.getElementsByTagName("property");
        for (int i=0;i<properties.getLength();i++) {
        	org.w3c.dom.Element property = (org.w3c.dom.Element)properties.item(i);
        	if (name.equals(property.getAttribute("name"))) {
        		sessionFactoryConfig.removeChild(property);
        		return;
        	}
        }
	}
    
    public static void configureHibernate(String connectionUrl) throws Exception {
        Properties properties = ApplicationProperties.getProperties();
        properties.setProperty("connection.url", connectionUrl);
        configureHibernate(properties);
    }
    
    public static String getProperty(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value!=null) {
            sLog.debug("   -- " + name + "=" + value);
        	return value;
        }
        sLog.debug("   -- using application properties for " + name);
        value = ApplicationProperties.getProperty(name);
        sLog.debug("     -- " + name + "=" + value);
        return value;
    }
    
    public static void fixSchemaInFormulas(Configuration cfg) {
        String schema = cfg.getProperty("default_schema"); 
        if (schema!=null) {
            for (Iterator i=cfg.getClassMappings();i.hasNext();) {
                PersistentClass pc = (PersistentClass)i.next();
                for (Iterator j=pc.getPropertyIterator();j.hasNext();) {
                    Property p = (Property)j.next();
                    for (Iterator k=p.getColumnIterator();k.hasNext();) {
                        Selectable c = (Selectable)k.next();
                        if (c instanceof Formula) {
                            Formula f = (Formula)c;
                            if (f.getFormula()!=null && f.getFormula().indexOf("%SCHEMA%")>=0) {
                                f.setFormula(f.getFormula().replaceAll("%SCHEMA%", schema));
                                sLog.debug("Schema updated in "+pc.getClassName()+"."+p.getName()+" to "+f.getFormula());
                            }
                        }
                    }
                }
            }
        }
    }

	public static void configureHibernate(Properties properties) throws Exception {
		if (sSessionFactory!=null) {
			sSessionFactory.close();
			sSessionFactory=null;
		}
		
		sLog.info("Connecting to "+getProperty(properties,"connection.url"));
		ClassLoader classLoader = HibernateUtil.class.getClassLoader();
		sLog.debug("  -- class loader retrieved");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		sLog.debug("  -- document factory created");
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
    	    public InputSource resolveEntity(String publicId, String systemId) {
    	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Mapping DTD//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd"));
        	    }
    	        return null;
    	    }
    	});
        sLog.debug("  -- document builder created");
        Document document = builder.parse(classLoader.getResource("hibernate.cfg.xml").openStream());
        sLog.debug("  -- hibernate.cfg.xml parsed");
        
        if (getProperty(properties,"connection.url")!=null) {
            removeProperty(document, "connection.datasource");
            String driver = getProperty(properties,"connection.driver_class");
            if (driver!=null) setProperty(document, "connection.driver_class", driver);
                setProperty(document, "connection.url",getProperty(properties,"connection.url"));
            String userName = getProperty(properties,"connection.username");
            if (userName!=null)
                setProperty(document, "connection.username",userName);
            String password = getProperty(properties,"connection.password");
            if (password!=null)
                setProperty(document, "connection.password", password);
            setProperty(document, "hibernate.jdbc.batch_size", "100");
            setProperty(document, "hibernate.cache.use_second_level_cache", "false");
            String dialect = getProperty(properties, "dialect");
            if (dialect!=null)
                setProperty(document, "dialect", dialect);
            String idgen = getProperty(properties, "tmtbl.uniqueid.generator");
            if (idgen!=null)
                setProperty(document, "tmtbl.uniqueid.generator", idgen);

            /*// JDBC Pool 
            setProperty(document, "connection.pool_size", "5");
            setProperty(document, "connection.release_mode", "on_close");
            */
            
            /*// C3P0 Pool
            setProperty(document, "hibernate.c3p0.min_size", "0");
            setProperty(document, "hibernate.c3p0.max_size", "5");
            setProperty(document, "hibernate.c3p0.timeout", "1800");
            setProperty(document, "hibernate.c3p0.max_statements", "50");
            setProperty(document, "hibernate.c3p0.validate", "true");
            */
            
            // Apache DBCP Pool
            setProperty(document, "hibernate.connection.provider_class", "org.unitime.commons.hibernate.connection.DBCPConnectionProvider");
            setProperty(document, "hibernate.dbcp.maxIdle", "2");
            setProperty(document, "hibernate.dbcp.maxActive", "5");
            setProperty(document, "hibernate.dbcp.whenExhaustedAction", "1");
            setProperty(document, "hibernate.dbcp.maxWait", "180000");
            setProperty(document, "hibernate.dbcp.testOnBorrow", "true");
            setProperty(document, "hibernate.dbcp.testOnReturn", "false");
            setProperty(document, "hibernate.dbcp.validationQuery", "select 1 from dual");
        }
        
        String default_schema = getProperty(properties, "default_schema");
        if (default_schema!=null)
            setProperty(document, "default_schema", default_schema);

        sLog.debug("  -- hibernate.cfg.xml altered");
        
        Configuration cfg = new Configuration();
        sLog.debug("  -- configuration object created");
        
    	cfg.setEntityResolver(new EntityResolver() {
    	    public InputSource resolveEntity(String publicId, String systemId) {
    	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Mapping DTD//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd"));
        	    }
    	        return null;
    	    }
    	});
        sLog.debug("  -- added entity resolver");
        
        cfg.configure(document);
        sLog.debug("  -- hibernate configured");

        fixSchemaInFormulas(cfg);
        
        UniqueIdGenerator.configure(cfg);

        sSessionFactory = cfg.buildSessionFactory();
        sLog.debug("  -- session factory created");
        (new _BaseRootDAO() {
    		void setSF(SessionFactory fact, Configuration cfg) {
    			_BaseRootDAO.sSessionFactory = fact;
    			_BaseRootDAO.sConfiguration = cfg;
    		}
    		protected Class getReferenceClass() { return null; }
    	}).setSF(sSessionFactory, cfg);
        sLog.debug("  -- session factory set to _BaseRootDAO");
        
        DatabaseUpdate.update();
    }
    
    public static void closeHibernate() {
		if (sSessionFactory!=null) {
			sSessionFactory.close();
			sSessionFactory=null;
		}
	}
    
    public static void configureHibernateFromRootDAO(String cfgName, Configuration cfg) {
        try {
        	EntityResolver entityResolver = new EntityResolver() {
        	    public InputSource resolveEntity(String publicId, String systemId) {
        	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd"));
        	        } else if (publicId.equals("-//Hibernate/Hibernate Mapping DTD//EN")) {
            	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-mapping-3.0.dtd"));
        	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd"));
        	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("hibernate-configuration-3.0.dtd"));
            	    }
        	        return null;
        	    }
        	};
        	
        	cfg.setEntityResolver(entityResolver);
            sLog.debug("  -- added entity resolver");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            sLog.debug("  -- document factory created");
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);
            sLog.debug("  -- document builder created");
            Document document = builder.parse(ConfigHelper.getConfigStream(cfgName==null?"hibernate.cfg.xml":cfgName));
            
            String dialect = ApplicationProperties.getProperty("dialect");
            if (dialect!=null) setProperty(document, "dialect", dialect);
            
            String default_schema = ApplicationProperties.getProperty("default_schema");
            if (default_schema!=null) setProperty(document, "default_schema", default_schema);
            
            String idgen = ApplicationProperties.getProperty("tmtbl.uniqueid.generator");
            if (idgen!=null) setProperty(document, "tmtbl.uniqueid.generator", idgen);
            
            for (Enumeration e=ApplicationProperties.getProperties().propertyNames();e.hasMoreElements();) {
                String name = (String)e.nextElement();
                if (name.startsWith("hibernate.") || name.startsWith("connection.") || name.startsWith("tmtbl.hibernate.")) {
                    String value = ApplicationProperties.getProperty(name);
                    if ("NULL".equals(value))
                        removeProperty(document, name);
                    else
                        setProperty(document, name, value);
                    if (!name.equals("connection.password"))
                        sLog.debug("  -- set "+name+": "+value);
                    else
                        sLog.debug("  -- set "+name+": *****");
                }
            }

            cfg.configure(document);
            sLog.debug("  -- hibernate configured");
            
            HibernateUtil.fixSchemaInFormulas(cfg);
            sLog.debug("  -- %SCHEMA% in formulas changed to "+cfg.getProperty("default_schema"));
            
            UniqueIdGenerator.configure(cfg);
            sLog.debug("  -- UniquId generator configured");
        } catch (Exception e) {
            sLog.error("Unable to configure hibernate, reason: "+e.getMessage(),e);
        }
    }
    
    private static String sConnectionUrl = null;
    
    public static String getConnectionUrl() {
        if (sConnectionUrl==null) {
            try {
                SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
                Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
                sConnectionUrl = connection.getMetaData().getURL();
                hibSessionFactory.getConnectionProvider().closeConnection(connection);
            } catch (Exception e) {
                sLog.error("Unable to get connection string, reason: "+e.getMessage(),e);
            }
        }
        return sConnectionUrl;
    }

    public static String getDatabaseName() {
        String schema = _RootDAO.getConfiguration().getProperty("default_schema");
        String url = getConnectionUrl();
        if (url==null) return "N/A";
        if (url.startsWith("jdbc:oracle:")) {
            return schema+"@"+url.substring(1+url.lastIndexOf(':'));
        }
        return schema;
    }
    
    public static void clearCache() {
        clearCache(null, true);
    }
    
    public static void clearCache(Class persistentClass) {
        clearCache(persistentClass, false);
    }

    public static void clearCache(Class persistentClass, boolean evictQueries) {
        _RootDAO dao = new _RootDAO();
        org.hibernate.Session hibSession = dao.getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory();
        if (persistentClass==null) {
            for (Iterator i=hibSessionFactory.getAllClassMetadata().entrySet().iterator();i.hasNext();) {
                Map.Entry entry = (Map.Entry)i.next();
                String className = (String)entry.getKey();
                ClassMetadata classMetadata = (ClassMetadata)entry.getValue();
                try {
                    hibSessionFactory.getCache().evictEntityRegion(Class.forName(className));
                    for (int j=0;j<classMetadata.getPropertyNames().length;j++) {
                        if (classMetadata.getPropertyTypes()[j].isCollectionType()) {
                            try {
                                hibSessionFactory.getCache().evictCollectionRegion(className+"."+classMetadata.getPropertyNames()[j]);
                            } catch (MappingException e) {}
                        }
                    }
                } catch (ClassNotFoundException e) {}
            }
        } else {
            ClassMetadata classMetadata = hibSessionFactory.getClassMetadata(persistentClass);
            hibSessionFactory.getCache().evictEntityRegion(persistentClass);
            if (classMetadata!=null) {
                for (int j=0;j<classMetadata.getPropertyNames().length;j++) {
                    if (classMetadata.getPropertyTypes()[j].isCollectionType()) {
                        try {
                            hibSessionFactory.getCache().evictCollectionRegion(persistentClass.getClass().getName()+"."+classMetadata.getPropertyNames()[j]);
                        } catch (MappingException e) {}
                    }
                }
            }
        }
        if (evictQueries)
            hibSessionFactory.getCache().evictQueryRegions();
    }
    
    public static Class<?> getDialect() {
    	try {
    		return Class.forName(_RootDAO.getConfiguration().getProperty("dialect"));
    	} catch (ClassNotFoundException e) {
    		return null;
    	}
    }
    
    public static boolean isMySQL() {
    	return MySQLDialect.class.isAssignableFrom(getDialect());
    }
    
    public static boolean isOracle() {
    	return Oracle8iDialect.class.isAssignableFrom(getDialect());
    }
    
    public static String addDate(String dateSQL, String incrementSQL) {
        if (isMySQL())
            return "adddate("+dateSQL+","+incrementSQL+")";
        else
            return dateSQL+(incrementSQL.startsWith("+")||incrementSQL.startsWith("-")?"":"+")+incrementSQL;
    }
    
    public static String dayOfWeek(String field) {
    	if (isOracle())
    		return "to_char(" + field + ",'D')";
    	else
    		return "dayofweek(" + field + ")";
    }
}