package edu.columbia.rdf.matcalc.toolbox.dna;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.jebtk.bioinformatics.dna.GenomeAssemblyLocal;
import org.jebtk.bioinformatics.dna.GenomeAssemblyWeb;
import org.jebtk.bioinformatics.dna.Sequence;
import org.jebtk.bioinformatics.genomic.GenomeAssembly;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.bioinformatics.genomic.SequenceRegion;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.TextUtils;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.math.matrix.DataFrame;
import org.jebtk.modern.UIService;
import org.jebtk.modern.dialog.ModernMessageDialog;
import org.jebtk.modern.event.ModernClickEvent;
import org.jebtk.modern.event.ModernClickListener;
import org.jebtk.modern.graphics.icons.RunVectorIcon;
import org.jebtk.modern.help.GuiAppInfo;
import org.jebtk.modern.ribbon.Ribbon;
import org.jebtk.modern.ribbon.RibbonLargeButton;
import org.jebtk.modern.status.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.columbia.rdf.matcalc.MainMatCalcWindow;
import edu.columbia.rdf.matcalc.bio.FastaReaderModule;
import edu.columbia.rdf.matcalc.toolbox.CalcModule;
import edu.columbia.rdf.matcalc.toolbox.dna.app.DnaInfo;

public class DnaModule extends CalcModule {
	public static final Logger LOG = 
			LoggerFactory.getLogger(DnaModule.class);

	private MainMatCalcWindow mWindow;

	//private DnaOptionsRibbonSection mDnaSection = 
	//		new DnaOptionsRibbonSection();

	private static final Path RES_DIR = PathUtils.getPath("res/modules/dna");

	//private ModernCheckBox mCheckIndels = new ModernCheckBox("Indels");

	static {
		// We only want to load the assemblies once so that each invocation
		// of the module does not trigger them to be loaded repeatedly.

		if (SettingsService.getInstance().getAsBool("org.matcalc.toolbox.bio.dna.web.enabled")) {
			try {
				DnaService.getInstance().add(new GenomeAssemblyWeb(new URL(SettingsService.getInstance().getAsString("dna.remote-url"))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Prefer local over web
		DnaService.getInstance().add(new GenomeAssemblyLocal(RES_DIR));
	}
	
	public DnaModule() {
		registerFileModule(new FastaWriterModule());
	}

	@Override
	public String getName() {
		return "DNA";
	}

	@Override
	public GuiAppInfo getModuleInfo() {
		return new DnaInfo();
	}

	@Override
	public void init(MainMatCalcWindow window) {
		mWindow = window;

		Ribbon ribbon = window.getRibbon();

		RibbonLargeButton button = new RibbonLargeButton("DNA", 
				UIService.getInstance().loadIcon(RunVectorIcon.class, 24),
				"DNA",
				"Extract the DNA for regions.");
		
		ribbon.getToolbar("DNA").getSection("DNA").add(button);
		//ribbon.getToolbar("DNA").getSection("DNA").add(UI.createHGap(5));
		//ribbon.getToolbar("DNA").getSection("DNA").add(new RibbonStripContainer(mCheckIndels));

		//mDnaSection.addClickListener(this);
		button.addClickListener(new ModernClickListener(){

			@Override
			public void clicked(ModernClickEvent e) {
				try {
					dna();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}});
		
		//ribbon.getToolbar("DNA").getSection("DNA").addSeparator();
		//ribbon.getToolbar("DNA").getSection("DNA").add(UI.createHGap(5));
		
		button = new RibbonLargeButton(UIService.getInstance().loadIcon("rev_comp", 24),
				"Reverse Complement DNA",
				"Reverse Complement DNA.");
		
		ribbon.getToolbar("DNA").getSection("DNA").add(button);
		
		button.addClickListener(new ModernClickListener(){

			@Override
			public void clicked(ModernClickEvent e) {
				revComp();
			}});
	}

	@Override
	public void run(String... args) {
		mWindow.getRibbon().setSelectedTab("DNA");

		try {
			dna();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new matrix and add columns containing the DNA for each
	 * row. The first column of the matrix being annotated should contain
	 * genomic coordinates.
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	private void dna() throws IOException, ParseException {
		/*
		List<Integer> columns = mWindow.getSelectedColumns();

		if (columns.size() == 0) {
			ModernMessageDialog.createWarningDialog(mWindow,
					"You must select a location column.");

			return;
		}

		int c = columns.get(0);
		 */

		DataFrame m = mWindow.getCurrentMatrix();
		
		if (m == null) {
			showLoadMatrixError(mWindow);
			return;
		}

		int locCol = TextUtils.findFirst(m.getColumnNames(), "Location");

		int chrCol = -1;
		int startCol = -1;
		int endCol = -1;

		if (locCol == -1) {
			chrCol = TextUtils.findFirst(m.getColumnNames(), "Chr", "Chrom");
			startCol = TextUtils.findFirst(m.getColumnNames(), "Start", "Position");

			endCol = TextUtils.findFirst(m.getColumnNames(), "End");
		}

		if (locCol == -1 && chrCol == -1) {
			ModernMessageDialog.createWarningDialog(mWindow, 
					"You must create a location column or chr, start, and end columns.");

			return;
		}

		List<GenomicRegion> regions = new ArrayList<GenomicRegion>(m.getRowCount());

		for (int i = 0; i < m.getRowCount(); ++i) {
			if (locCol != -1) {
				regions.add(GenomicRegion.parse(m.getText(i, locCol)));
			} else {
				if (endCol != -1) {
					regions.add(GenomicRegion.parse(m.getText(i, chrCol), m.getText(i, startCol), m.getText(i, endCol)));
				} else {
					// Same start and end

					regions.add(GenomicRegion.parse(m.getText(i, chrCol), m.getText(i, startCol)));

					System.err.println(regions.get(regions.size() - 1).getChr() + " " + regions.get(regions.size() - 1).getStart());
				}
			}
		}

		DnaDialog dialog = new DnaDialog(mWindow);

		dialog.setVisible(true);

		if (dialog.isCancelled()) {
			return;
		}

		String genome = dialog.getGenome();
		GenomeAssembly assembly = dialog.getAssembly();

		StatusService.getInstance().setStatus("Extending regions...");
		LOG.info("Extending regions...");

		if (dialog.getFromCenter()) {
			// Center sequences

			regions = GenomicRegion.center(regions);
		}

		int offset5p = dialog.getOffset5p(); //mDnaSection.getOffset5p();
		int offset3p = dialog.getOffset3p(); //mDnaSection.getOffset3p();

		// Extend if necessary
		List<GenomicRegion> extendedRegions = 
				GenomicRegion.extend(regions, offset5p, offset3p);

		StatusService.getInstance().setStatus("Extracting DNA sequences...");
		
		LOG.info("Extracting DNA sequences using {}...", assembly.getName());

		RepeatMaskType repeatMaskType = dialog.getRepeatMaskType(); //mDnaSection.getRepeatMaskType();

		boolean uppercase = dialog.getDisplayUpper();
		
		List<SequenceRegion> sequences = assembly.getSequences(genome, 
						extendedRegions,
						uppercase,
						repeatMaskType);
		

		//
		// Cope with insertions and deletions
		//

		List<SequenceRegion> indelSequences = null;

		/*
		if (mCheckIndels.isSelected()) {
			int refCol = TextUtils.findFirst(m.getColumnNames(), "Ref");
			int obsCol = TextUtils.findFirst(m.getColumnNames(), "Obs");

			if (refCol != -1 && obsCol != -1) {
				indelSequences = 
						new ArrayList<SequenceRegion>(sequences.size());

				for (int i = 0; i < m.getRowCount(); ++i) {
					GenomicRegion region = regions.get(i);
					GenomicRegion extRegion = extendedRegions.get(i);

					// Where the the indel goes
					int offset = region.getStart() - extRegion.getStart();

					SequenceRegion seq = sequences.get(i);

					String bases = seq.getSequence();

					// Remove the dash char
					String ref = m.getText(i, refCol);
					String obs = m.getText(i, obsCol);

					StringBuilder buffer = new StringBuilder();

					buffer.append(bases.substring(0, offset));

					int start = extRegion.getStart();
					int end = extRegion.getEnd();

					if (ref.length() > obs.length()) {
						// deletion
						buffer.append(obs.replace("-", ""));

						int l = ref.length() - obs.length() + 1;

						// Add the rest of the sequence
						buffer.append(bases.substring(offset + ref.length()));

						end -= l;
					} else if (ref.length() < obs.length()) {
						// insertion
						buffer.append(obs);

						end += obs.length() - ref.length() + 1;

						buffer.append(bases.substring(offset + 1));
					} else {
						// mutation

						buffer.append(obs);

						buffer.append(bases.substring(offset + obs.length()));
					}

					indelSequences.add(new SequenceRegion(new GenomicRegion(extRegion.getChr(), start, end), buffer.toString()));
				}
			}
		}
		*/

		// There were no indels so the sequences remain unchanged.
		if (indelSequences == null) {
			indelSequences = sequences;
		}


		List<SequenceRegion> revCompSeqs = null;

		if (dialog.getRevComp()) {
			revCompSeqs = SequenceRegion.reverseComplementRegion(indelSequences);
		} else {
			revCompSeqs = indelSequences;
		}

		StatusService.getInstance().setStatus("Creating matrix...");
		LOG.info("Creating matrix...");

		int n = m.getColumnCount();

		DataFrame ret = 
				DataFrame.createDataFrame(m.getRowCount(), n + 4);

		DataFrame.copyColumns(m, ret, 0);

		ret.setColumnName(n, "DNA Location");
		ret.setColumnName(n + 1, "DNA Sequence");
		ret.setColumnName(n + 2, "Length (bp)");
		ret.setColumnName(n + 3, "Options");

		List<String> options = new ArrayList<String>(4);
		
		options.add("strand=" + (dialog.getRevComp() ? "-" : "+"));
		options.add("repeat-mask=" + repeatMaskType.toString().toLowerCase());
		options.add("5'-ext=" + offset5p);
		options.add("3'-ext=" + offset3p);
		
		String opts = Join.onSemiColon().values(options).toString();
		
		for (int i = 0; i < m.getRowCount(); ++i) {
			String seq = revCompSeqs.get(i).getSequence().toString();

			ret.set(i, n, revCompSeqs.get(i).getLocation());
			ret.set(i, n + 1, seq);
			ret.set(i, n + 2, seq.length());
			ret.set(i, n + 3, opts);
		}

		mWindow.addToHistory("Extract DNA", ret);

		StatusService.getInstance().setReady();
	}

	private void revComp() {
		DataFrame m = mWindow.getCurrentMatrix();
		
		List<Sequence> sequences = FastaWriterModule.toSequences(mWindow, m);
		
		List<Sequence> revComp = Sequence.reverseComplement(sequences);
		
		DataFrame ret = FastaReaderModule.toMatrix(revComp);
		
		mWindow.addToHistory("Reverse Complement", ret);
		
		StatusService.getInstance().setReady();
	}
}
