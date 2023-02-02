/*
 * Prolib CLI
 * Copyright (c) 2023 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package eu.rssw.openedge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import eu.rssw.pct.FileEntry;
import eu.rssw.pct.PLReader;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class ProlibCli {
  private static CommandExtract extract = new CommandExtract();
  private static CommandList list = new CommandList();
  private static CommandCompare compare = new CommandCompare();

  private PrintStream out;

  public ProlibCli() {
    this(System.out);
  }

  public ProlibCli(PrintStream out) {
    this.out = out;
  }

  public static void main(String[] args) {
    ProlibCli main = new ProlibCli();
    JCommander jc = new JCommander(main);
    jc.addCommand("extract", extract);
    jc.addCommand("list", list);
    jc.addCommand("compare", compare);

    try {
      jc.parse(args);
    } catch (ParameterException caught) {
      caught.printStackTrace();
      jc.usage();
      System.exit(1);
    }
    try {
      if ("list".equals(jc.getParsedCommand()))
        main.executeList();
      else if ("extract".equals(jc.getParsedCommand()))
        main.executeExtract();
      else if ("compare".equals(jc.getParsedCommand()))
        main.executeCompare();
    } catch (IOException caught) {
      main.out.println("I/O problem: " + caught.getMessage());
      System.exit(1);
    }
  }

  public void executeList() throws IOException {
    PLReader reader = new PLReader(list.lib.get(0));
    out.printf("%6s  %20s  %44s  %s%n", "CRC", "Timestamp", "Digest", "Size", "File name");
    for (FileEntry entry : reader.getFileList()) {
      try {
        RCodeInfo rci1 = new RCodeInfo(reader.getInputStream(entry));
        out.printf("%6s  %20s  %44s  %10d  %s%n", rci1.getCrc(),
            Instant.ofEpochMilli(rci1.getTimeStamp() * 1000).atOffset(ZoneOffset.UTC).format(
                DateTimeFormatter.ISO_INSTANT),
            rci1.getDigest(), entry.getSize(), entry.getFileName());
      } catch (InvalidRCodeException caught) {
        out.printf("%6s %44s %10d %s%n", "-", "-", entry.getSize(), entry.getFileName());
      }
    }
  }

  public void executeExtract() {
    PLReader reader = new PLReader(extract.lib.get(0));
    List<FileEntry> entries = reader.getFileList();
    if (reader.isMemoryMapped()) {
      out.println("Unable to extract files from memory-mapped library");
      return;
    }
    for (FileEntry entry : entries) {
      File file = new File(entry.getFileName().replace('\\', '/'));
      try (InputStream input = reader.getInputStream(entry)) {
        if (file.getParentFile() != null)
          file.getParentFile().mkdirs();
        Files.copy(input, file.toPath());
      } catch (IOException e) {
        out.printf("Unable to extract file %s%n", entry.getFileName());
      }
    }
  }

  public void executeCompare() throws IOException {
    PLReader lib1 = new PLReader(compare.libs.get(0));
    PLReader lib2 = new PLReader(compare.libs.get(1));

    List<FileEntry> list1 = lib1.getFileList();
    List<FileEntry> list2 = lib2.getFileList();
    Collections.sort(list1);
    Collections.sort(list2);

    for (FileEntry entry1 : list1) {
      int idx = list2.indexOf(entry1);
      if (idx >= 0) {
        try {
          RCodeInfo info1 = new RCodeInfo(lib1.getInputStream(entry1));
          RCodeInfo info2 = new RCodeInfo(lib2.getInputStream(list2.get(idx)));
          if ((info1.getCrc() == info2.getCrc()) && (info1.getDigest().equals(info2.getDigest()))) {
            if (compare.showIdenticals)
              out.println("I " + entry1.getFileName());
          } else {
            out.println("M " + entry1.getFileName());
          }
        } catch (InvalidRCodeException caught) {
          out.println("- " + entry1.getFileName());
        }
        list2.remove(idx);
      } else {
        out.println("A " + entry1.getFileName());
      }
    }
    for (FileEntry entry2 : list2) {
      out.println("R " + entry2.getFileName());
    }
  }

  @Parameters(commandDescription = "Extract PL")
  public static class CommandExtract {
    @Parameter(description = "PL file", required = true, arity = 1)
    private List<Path> lib;

    @Parameter(names = { "-p", "--pattern" }, description = "File patterns")
    private List<String> patterns;
  }

  @Parameters(commandDescription = "List PL content")
  public static class CommandList {
    @Parameter(description = "/path/to/file.pl", required = true, arity = 1)
    private List<Path> lib;

  }

  @Parameters(commandDescription = "Compare two PL")
  public static class CommandCompare {
    @Parameter(arity = 2, description = "Source and target PL files", required = true)
    private List<Path> libs;

    @Parameter(names =  { "-i", "--showIdenticals" }, description = "Also display identical files")
    private Boolean showIdenticals = false;
  }
}