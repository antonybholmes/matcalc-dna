package edu.columbia.rdf.matcalc.toolbox.dna.app;

import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;

import org.jebtk.core.AppService;
import org.jebtk.modern.ColorTheme;
import org.jebtk.modern.theme.ThemeService;
import org.xml.sax.SAXException;

import edu.columbia.rdf.matcalc.CoreModuleLoader;
import edu.columbia.rdf.matcalc.MainMatCalc;
import edu.columbia.rdf.matcalc.MatCalcProperties;
import edu.columbia.rdf.matcalc.ModuleService;
import edu.columbia.rdf.matcalc.bio.FastaReaderModule;
import edu.columbia.rdf.matcalc.bio.FastaWriterModule;
import edu.columbia.rdf.matcalc.bio.toolbox.external.ucsc.BedGraphIOModule;
import edu.columbia.rdf.matcalc.bio.toolbox.external.ucsc.BedIOModule;
import edu.columbia.rdf.matcalc.toolbox.dna.DnaModule;

public class MainDna {
  public static final void main(String[] args) throws FontFormatException,
  IOException, SAXException, ParserConfigurationException,
  ClassNotFoundException, InstantiationException, IllegalAccessException,
  UnsupportedLookAndFeelException {
    AppService.getInstance().setAppInfo("dna");

    //ModuleLoader ml = new BioModuleLoader().addModule(DnaModule.class);

    //ModuleService.getInstance().add(new BioModuleLoader());
    ModuleService.getInstance().add(DnaModule.class);
    ModuleService.getInstance().add(new CoreModuleLoader());
    ModuleService.getInstance().add(BedIOModule.class);
    ModuleService.getInstance().add(BedGraphIOModule.class);
    ModuleService.getInstance().add(FastaReaderModule.class);
    ModuleService.getInstance().add(FastaWriterModule.class);
   
    
    if (args.length > 0) {
      // Run a module
      ModuleService.getInstance().instance("DNA").run(args);
    } else {
      // Load the UI

      ThemeService.getInstance().setTheme(ColorTheme.GREEN);

      MatCalcProperties props = new MatCalcProperties();
      props.setProperty("matcalc.ui.files.enabled", false);
      props.setProperty("matcalc.ui.left-tabs.enabled", false);
      props.setProperty("matcalc.ui.right-tabs.enabled", false);
      props.setProperty("matcalc.ui.table.drop-shadow.enabled", false);
      
      MainMatCalc.main(new DnaInfo(), props);
    }
    

    
  }
}
