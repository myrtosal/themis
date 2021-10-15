/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.csd.uoc.hy463.themis.ui;

import gr.csd.uoc.hy463.themis.config.Config;
import gr.csd.uoc.hy463.themis.indexer.Indexer;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Myrto
 */
public class CreateIndex {
    
    Indexer index; 
    private static final Logger __LOGGER__ = LogManager.getLogger(CreateIndex.class);
    Config __CONFIG__ ;
    public CreateIndex(Indexer index, Config __CONFIG__) {
            this.index = index; 
            this.__CONFIG__ = __CONFIG__;
    }    
        
    public void createIndex(Boolean bool) {
        
     
            long startTime = System.currentTimeMillis();
  
           
            try {
				index.index(bool);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
            
            long endTime = System.currentTimeMillis();
            
            long timeElapsed = endTime - startTime;
            
            __LOGGER__.info("EXEC TIME: " + timeElapsed / 1000);
        
        
    }
        
}
