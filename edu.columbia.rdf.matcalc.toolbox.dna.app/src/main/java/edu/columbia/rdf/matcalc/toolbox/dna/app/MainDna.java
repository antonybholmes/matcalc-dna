package edu.columbia.rdf.matcalc.toolbox.dna.app;

import java.awt.FontFormatException;
import java.io.IOException;

import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;

import org.jebtk.core.AppService;
import org.jebtk.modern.ColorTheme;
import org.jebtk.modern.theme.ThemeService;
import org.xml.sax.SAXException;

import edu.columbia.rdf.matcalc.MainMatCalc;
import edu.columbia.rdf.matcalc.ModuleLoader;
import edu.columbia.rdf.matcalc.bio.BioModuleLoader;
import edu.columbia.rdf.matcalc.toolbox.dna.DnaModule;




public class MainDna {
	public static final void main(String[] args) throws FontFormatException, IOException, SAXException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		AppService.getInstance().setAppInfo("dna");
		
		ThemeService.getInstance().setTheme(ColorTheme.GREEN);
		
		ModuleLoader ml = new BioModuleLoader()
				.addModule(DnaModule.class);
		
		MainMatCalc.main(new DnaInfo(), ml);
	}
}
