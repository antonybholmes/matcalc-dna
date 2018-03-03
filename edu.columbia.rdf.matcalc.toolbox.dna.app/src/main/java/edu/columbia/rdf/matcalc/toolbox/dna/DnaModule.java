package edu.columbia.rdf.matcalc.toolbox.dna;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jebtk.bioinformatics.dna.GenomeAssemblyFS;
import org.jebtk.bioinformatics.dna.GenomeAssemblyWeb;
import org.jebtk.bioinformatics.dna.GenomeAssemblyZip;
import org.jebtk.bioinformatics.dna.Sequence;
import org.jebtk.bioinformatics.ext.ucsc.Bed;
import org.jebtk.bioinformatics.ext.ucsc.UCSCTrackRegion;
import org.jebtk.bioinformatics.genomic.Dna;
import org.jebtk.bioinformatics.genomic.Genome;
import org.jebtk.bioinformatics.genomic.GenomeAssembly;
import org.jebtk.bioinformatics.genomic.GenomeAssemblyService;
import org.jebtk.bioinformatics.genomic.GenomeService;
import org.jebtk.bioinformatics.genomic.GenomicRegion;
import org.jebtk.bioinformatics.genomic.RepeatMaskType;
import org.jebtk.bioinformatics.genomic.SequenceRegion;
import org.jebtk.core.Range;
import org.jebtk.core.cli.CommandLineArg;
import org.jebtk.core.cli.CommandLineArgs;
import org.jebtk.core.cli.Options;
import org.jebtk.core.collections.CollectionUtils;
import org.jebtk.core.io.PathUtils;
import org.jebtk.core.settings.SettingsService;
import org.jebtk.core.sys.SysUtils;
import org.jebtk.core.text.Join;
import org.jebtk.core.text.TextUtils;
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
import edu.columbia.rdf.matcalc.OpenMode;
import edu.columbia.rdf.matcalc.bio.FastaReaderModule;
import edu.columbia.rdf.matcalc.bio.FastaWriterModule;
import edu.columbia.rdf.matcalc.toolbox.CalcModule;
import edu.columbia.rdf.matcalc.toolbox.dna.app.DnaInfo;

public class DnaModule extends CalcModule {
  public static final Logger LOG = LoggerFactory.getLogger(DnaModule.class);

  private MainMatCalcWindow mWindow;

  // private DnaOptionsRibbonSection mDnaSection =
  // new DnaOptionsRibbonSection();

  // private ModernCheckBox mCheckIndels = new ModernCheckBox("Indels");

  static {
    // We only want to load the assemblies once so that each invocation
    // of the module does not trigger them to be loaded repeatedly.

    if (SettingsService.getInstance()
        .getAsBool("org.matcalc.toolbox.bio.dna.web.enabled")) {
      try {
        GenomeAssemblyService.instance().add(new GenomeAssemblyWeb(new URL(
            SettingsService.getInstance().getAsString("dna.remote-url"))));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    GenomeAssemblyService.instance().add(new GenomeAssemblyFS(Dna.RES_DIR));

    // Prefer local over web
    GenomeAssemblyService.instance().add(new GenomeAssemblyZip(Dna.RES_DIR));
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
        UIService.getInstance().loadIcon(RunVectorIcon.class, 24), "DNA",
        "Extract the DNA for regions.");

    ribbon.getToolbar("DNA").getSection("DNA").add(button);
    // ribbon.getToolbar("DNA").getSection("DNA").add(UI.createHGap(5));
    // ribbon.getToolbar("DNA").getSection("DNA").add(new
    // RibbonStripContainer(mCheckIndels));

    // mDnaSection.addClickListener(this);
    button.addClickListener(new ModernClickListener() {

      @Override
      public void clicked(ModernClickEvent e) {
        try {
          dna(Genome.HG19);
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });

    // ribbon.getToolbar("DNA").getSection("DNA").addSeparator();
    // ribbon.getToolbar("DNA").getSection("DNA").add(UI.createHGap(5));

    button = new RibbonLargeButton(
        UIService.getInstance().loadIcon("rev_comp", 24),
        "Reverse Complement DNA", "Reverse Complement DNA.");

    ribbon.getToolbar("DNA").getSection("DNA").add(button);

    button.addClickListener(new ModernClickListener() {

      @Override
      public void clicked(ModernClickEvent e) {
        revComp(Genome.HG19);
      }
    });

    button = new RibbonLargeButton(
        UIService.getInstance().loadIcon("random", 24),
        "Random DNA", "Create random DNA sequences.");

    ribbon.getToolbar("DNA").getSection("DNA").add(button);

    button.addClickListener(new ModernClickListener() {

      @Override
      public void clicked(ModernClickEvent e) {
        try {
          randomDna();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
  }

  @Override
  public void run(String... args) {
    String genome = "grch38";

    Path zipDir = PathUtils.getPwd();
    Path genomeDir = PathUtils.getPwd();
    Path file = null;

    int n = 200;
    int l = 200;

    // first argument is type such as random
    String mode = args[0].toLowerCase(); // random

    // all other arguments are interpreted as standard posix
    String[] modArgs = new String[args.length - 1];
    SysUtils.arraycopy(args, 1, modArgs);

    System.err.println(Arrays.toString(modArgs));

    Options options = new Options()
        .add('f', "file", true)
        .add('g', "genome", true)
        .add('m', "mode", true)
        .add('n', "n", true)
        .add('l', "length", true)
        .add('d', "genome-dir", true)
        .add('z', "zip-dir", true);

    CommandLineArgs cmdArgs = CommandLineArgs.parse(options, modArgs);



    for (CommandLineArg cmdArg : cmdArgs) {
      switch (cmdArg.getShortName()) {
      case 'f':
        file = PathUtils.getPath(cmdArg.getValue());
        break;
      case 'm':
        mode = cmdArg.getValue();
        break;
      case 'n':
        n = cmdArg.getIntValue();
        break;
      case 'l':
        l = cmdArg.getIntValue();
        break;
      case 'g':
        genome = cmdArg.getValue();
        break;
      case 'd':
        genomeDir = PathUtils.getPath(cmdArg.getValue());
        break;
      case 'z':
        zipDir = PathUtils.getPath(cmdArg.getValue());
        break;
      }
    }

    LOG.info("dna {}: {} {} {} {}", mode, genome, l, n, zipDir);

    GenomeService.getInstance().setDir(genomeDir);

    GenomeAssembly assembly = null;
    
    if (zipDir != null) {
      assembly = new GenomeAssemblyZip(zipDir);
    }
    
    if (mode.startsWith("seq")) {
      try {
        cmdBed(genome, file, assembly);
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (mode.startsWith("rand")) {
      cmdRand(genome, l, n, assembly);
    } else if (mode.startsWith("encode")) {
      try {
        encode(genome, genomeDir);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void encode(String genome, Path dir) throws IOException {
    EncodeExt2Bit.encodeGenome(genome, dir);
  }

  private static void cmdBed(String genome, Path file, GenomeAssembly assembly) throws IOException {
    if (file == null) {
      return;
    }

    Bed bed = Bed.parseBedGraph(file);

    List<UCSCTrackRegion> regions = CollectionUtils.sort(bed.getRegions());

    cmdOutputSeqs(genome, regions, assembly);
  }

  private static void cmdRand(String genome, int l, int n, GenomeAssembly assembly) {


    List<GenomicRegion> regions = new ArrayList<GenomicRegion>(n);

    try {

      for (int i : Range.create(n)) {
        regions.add(GenomicRegion.randomRegion(genome, l));
      }

      //Sort to speed up retrival
      Collections.sort(regions);

      cmdOutputSeqs(genome, regions, assembly);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void cmdOutputSeqs(String genome,
      List<? extends GenomicRegion> regions, GenomeAssembly assembly) throws IOException {
    for (GenomicRegion region : regions) {
      SequenceRegion seq = assembly.getSequence(genome, region);

      System.out.println(seq.toFasta());
    }
  }

  /**
   * Create a new matrix and add columns containing the DNA for each row. The
   * first column of the matrix being annotated should contain genomic
   * coordinates.
   * 
   * @throws IOException
   * @throws ParseException
   */
  private void dna(String genome) throws IOException {
    /*
     * List<Integer> columns = mWindow.getSelectedColumns();
     * 
     * if (columns.size() == 0) {
     * ModernMessageDialog.createWarningDialog(mWindow,
     * "You must select a location column.");
     * 
     * return; }
     * 
     * int c = columns.get(0);
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

    List<GenomicRegion> regions = new ArrayList<GenomicRegion>(m.getRows());

    for (int i = 0; i < m.getRows(); ++i) {
      if (locCol != -1) {
        regions.add(GenomicRegion.parse(genome, m.getText(i, locCol)));
      } else {
        if (endCol != -1) {
          regions.add(GenomicRegion.parse(genome, m.getText(i, chrCol),
              m.getText(i, startCol),
              m.getText(i, endCol)));
        } else {
          // Same start and end

          regions.add(GenomicRegion.parse(m.getText(i, chrCol),
              m.getText(i, startCol)));

          System.err.println(regions.get(regions.size() - 1).getChr() + " "
              + regions.get(regions.size() - 1).getStart());
        }
      }
    }

    DnaDialog dialog = new DnaDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    genome = dialog.getGenome();
    GenomeAssembly assembly = dialog.getAssembly();

    StatusService.getInstance().setStatus("Extending regions...");
    LOG.info("Extending regions...");

    if (dialog.getFromCenter()) {
      // Center sequences

      regions = GenomicRegion.center(regions);
    }

    int offset5p = dialog.getOffset5p(); // mDnaSection.getOffset5p();
    int offset3p = dialog.getOffset3p(); // mDnaSection.getOffset3p();

    // Extend if necessary
    List<GenomicRegion> extendedRegions = GenomicRegion
        .extend(regions, offset5p, offset3p);

    StatusService.getInstance().setStatus("Extracting DNA sequences...");

    LOG.info("Extracting DNA sequences using {}...", assembly.getName());

    RepeatMaskType repeatMaskType = dialog.getRepeatMaskType(); // mDnaSection.getRepeatMaskType();

    boolean uppercase = dialog.getDisplayUpper();

    List<SequenceRegion> sequences = assembly
        .getSequences(genome, extendedRegions, uppercase, repeatMaskType);

    //
    // Cope with insertions and deletions
    //

    List<SequenceRegion> indelSequences = null;

    /*
     * if (mCheckIndels.isSelected()) { int refCol =
     * TextUtils.findFirst(m.getColumnNames(), "Ref"); int obsCol =
     * TextUtils.findFirst(m.getColumnNames(), "Obs");
     * 
     * if (refCol != -1 && obsCol != -1) { indelSequences = new
     * ArrayList<SequenceRegion>(sequences.size());
     * 
     * for (int i = 0; i < m.getRowCount(); ++i) { GenomicRegion region =
     * regions.get(i); GenomicRegion extRegion = extendedRegions.get(i);
     * 
     * // Where the the indel goes int offset = region.getStart() -
     * extRegion.getStart();
     * 
     * SequenceRegion seq = sequences.get(i);
     * 
     * String bases = seq.getSequence();
     * 
     * // Remove the dash char String ref = m.getText(i, refCol); String obs =
     * m.getText(i, obsCol);
     * 
     * StringBuilder buffer = new StringBuilder();
     * 
     * buffer.append(bases.substring(0, offset));
     * 
     * int start = extRegion.getStart(); int end = extRegion.getEnd();
     * 
     * if (ref.length() > obs.length()) { // deletion
     * buffer.append(obs.replace("-", ""));
     * 
     * int l = ref.length() - obs.length() + 1;
     * 
     * // Add the rest of the sequence buffer.append(bases.substring(offset +
     * ref.length()));
     * 
     * end -= l; } else if (ref.length() < obs.length()) { // insertion
     * buffer.append(obs);
     * 
     * end += obs.length() - ref.length() + 1;
     * 
     * buffer.append(bases.substring(offset + 1)); } else { // mutation
     * 
     * buffer.append(obs);
     * 
     * buffer.append(bases.substring(offset + obs.length())); }
     * 
     * indelSequences.add(new SequenceRegion(new
     * GenomicRegion(extRegion.getChr(), start, end), buffer.toString())); } } }
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

    int n = m.getCols();

    DataFrame ret = DataFrame.createDataFrame(m.getRows(), n + 5);

    DataFrame.copyColumns(m, ret, 0);

    ret.setColumnName(n, "Genome");
    ret.setColumnName(n + 1, "DNA Location");
    ret.setColumnName(n + 2, "DNA Sequence");
    ret.setColumnName(n + 3, "Length (bp)");
    ret.setColumnName(n + 4, "Options");

    List<String> options = new ArrayList<String>(4);

    options.add("strand=" + (dialog.getRevComp() ? "-" : "+"));
    options.add("repeat-mask=" + repeatMaskType.toString().toLowerCase());
    options.add("5'-ext=" + offset5p);
    options.add("3'-ext=" + offset3p);

    String opts = Join.onSemiColon().values(options).toString();

    for (int i = 0; i < m.getRows(); ++i) {
      String seq = revCompSeqs.get(i).getSequence().toString();

      ret.set(i, n, genome);
      ret.set(i, n + 1, revCompSeqs.get(i).getLocation());
      ret.set(i, n + 2, seq);
      ret.set(i, n + 3, seq.length());
      ret.set(i, n + 4, opts);
    }

    mWindow.addToHistory("Extract DNA", ret);

    StatusService.getInstance().setReady();
  }

  private void randomDna() throws IOException {
    RandomDnaDialog dialog = new RandomDnaDialog(mWindow);

    dialog.setVisible(true);

    if (dialog.isCancelled()) {
      return;
    }

    String genome = dialog.getGenome();
    GenomeAssembly assembly = dialog.getAssembly();

    RepeatMaskType repeatMaskType = dialog.getRepeatMaskType(); // mDnaSection.getRepeatMaskType();

    boolean uppercase = dialog.getDisplayUpper();

    int n = dialog.getN();
    int length = dialog.getLength();

    List<SequenceRegion> seqs = randomDna(genome, assembly, length, n, uppercase, repeatMaskType);

    DataFrame ret = DataFrame.createDataFrame(n, 4);

    ret.setColumnName(0, "DNA Location");
    ret.setColumnName(1, "Length (bp)");
    ret.setColumnName(2, "Options");
    ret.setColumnName(3, "DNA Sequence");

    List<String> options = new ArrayList<String>(4);

    options.add("repeat-mask=" + repeatMaskType.toString().toLowerCase());

    String opts = Join.onSemiColon().values(options).toString();

    for (int i = 0; i < seqs.size(); ++i) {
      SequenceRegion seq = seqs.get(i);

      ret.set(i, 0, seq.getLocation());
      ret.set(i, 1, seq.getSequence().getLength());
      ret.set(i, 2, opts);
      ret.set(i, 3, seq.getSequence().toString());
    }

    ret.setName("Random DNA");

    mWindow.openMatrix(ret, OpenMode.NEW_WINDOW);
  }

  private static List<SequenceRegion> randomDna(String genome, 
      GenomeAssembly assembly, 
      int length, 
      int n) throws IOException {
    return randomDna(genome, assembly, length, n, true, RepeatMaskType.LOWERCASE);
  }

  private static List<SequenceRegion> randomDna(String genome, 
      GenomeAssembly assembly, 
      int length, 
      int n,
      boolean displayUpper,
      RepeatMaskType repeatMaskType) throws IOException {
    //genome

    List<GenomicRegion> regions = new ArrayList<GenomicRegion>(n);

    for (int i : Range.create(n)) {
      regions.add(GenomicRegion.randomRegion(genome, length));
    }

    //Sort to speed up retrival
    Collections.sort(regions);

    List<SequenceRegion> ret = new ArrayList<SequenceRegion>(n);

    for (GenomicRegion region : regions) {
      ret.add(assembly.getSequence(genome, region, displayUpper, repeatMaskType));
    }

    return ret;
  }

  private static SequenceRegion randomDna(String genome, 
      GenomeAssembly assembly, 
      int length) throws IOException {
    return randomDna(genome, assembly, length, true, RepeatMaskType.LOWERCASE);
  }

  private static SequenceRegion randomDna(String genome, 
      GenomeAssembly assembly, 
      int length,
      boolean displayUpper,
      RepeatMaskType repeatMaskType) throws IOException {
    //genome

    GenomicRegion region = GenomicRegion.randomRegion(genome, length);

    return assembly.getSequence(genome, region, displayUpper, repeatMaskType);
  }

  private void revComp(String genome) {
    DataFrame m = mWindow.getCurrentMatrix();

    List<Sequence> sequences = FastaWriterModule.toSequences(mWindow, m);

    List<Sequence> revComp = Sequence.reverseComplement(sequences);
    
    List<GenomicRegion> regions = toRegions(mWindow, genome, m);

    DataFrame ret = FastaReaderModule.toMatrix(genome, regions, revComp);

    ret.setName("Reverse Complement");

    mWindow.openMatrix(ret, OpenMode.NEW_WINDOW);
  }
  
  public static List<GenomicRegion> toRegions(final MainMatCalcWindow window,
      String genome,
      final DataFrame m) {

    int c1 = DataFrame.findColumn(m, "DNA Location");

    if (c1 == -1) {
      c1 = DataFrame.findColumn(m, "Location");
    }

    List<GenomicRegion> sequences = new ArrayList<GenomicRegion>(m.getRows());

    for (int i = 0; i < m.getRows(); ++i) {
      sequences.add(GenomicRegion.parse(genome, m.getText(i, c1)));
    }

    return sequences;
  }
}
