package id.seria.gacha.lib.xseries;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Note;
import org.bukkit.Note.Tone;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NoteBlockMusic {
   private static final Map<String, Instrument> INSTRUMENTS = new HashMap<>(50);
   private static final Map<Instrument, XSound> INSTRUMENT_TO_SOUND = new EnumMap<>(Instrument.class);

   private NoteBlockMusic() {
   }

   @NotNull
   public static XSound getSoundFromInstrument(@NotNull Instrument instrument) {
      return INSTRUMENT_TO_SOUND.get(instrument);
   }

   @Nullable
   public static Tone getNoteTone(char ch) {
      switch (ch) {
         case 'A':
            return Tone.A;
         case 'B':
            return Tone.B;
         case 'C':
            return Tone.C;
         case 'D':
            return Tone.D;
         case 'E':
            return Tone.E;
         case 'F':
            return Tone.F;
         case 'G':
            return Tone.G;
         default:
            return null;
      }
   }

   public static void testMusic(@NotNull Player player) {
      playMusic(player, player::getLocation, "PIANO,D,2,100 PIANO,B#1 200 PIANO,F 250 PIANO,E 250 PIANO,B 200 PIANO,A 100 PIANO,B 100 PIANO,E");
   }

   public static void fromFile(@NotNull Player player, @NotNull Supplier<Location> location, @NotNull Path path) {
      try {
         BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);

         String line;
         try {
            while ((line = reader.readLine()) != null) {
               line = line.trim();
               if (!line.isEmpty() && !line.startsWith("#")) {
                  parseInstructions(line).play(player, location, true);
               }
            }
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (IOException ex) {
         ex.printStackTrace();
      }
   }

   public static void playMusic(@NotNull Player player, @NotNull Supplier<Location> location, @Nullable String script) {
      if (!Strings.isNullOrEmpty(script)) {
         NoteBlockMusic.Sequence seq = parseInstructions(script);
         seq.play(player, location, true);
      }
   }

   public static NoteBlockMusic.Sequence parseInstructions(@NotNull CharSequence script) {
      return (new NoteBlockMusic.InstructionBuilder(script)).sequence;
   }

   private static void sleep(long fermata) {
      try {
         Thread.sleep(fermata);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }

   @Nullable
   public static Note parseNote(@NotNull String note) {
      Tone tone = getNoteTone((char)(note.charAt(0) & 95));
      if (tone == null) {
         return null;
      }

      int len = note.length();
      char toneType = ' ';
      int octave = 0;
      if (len > 1) {
         toneType = note.charAt(1);
         if (isDigit(toneType)) {
            octave = toneType - '0';
         } else if (len > 2) {
            char octaveDigit = note.charAt(2);
            if (isDigit(octaveDigit)) {
               octave = octaveDigit - '0';
            }
         }

         if (octave < 0 || octave > 2) {
            octave = 0;
         }
      }

      return toneType == '#' ? Note.sharp(octave, tone) : (toneType == '_' ? Note.flat(octave, tone) : Note.natural(octave, tone));
   }

   private static boolean isDigit(char ch) {
      return ch >= '0' && ch <= '9';
   }

   public static float noteToPitch(@NotNull Note note) {
      return (float)Math.pow(2.0, (note.getId() - 12.0) / 12.0);
   }

   @NotNull
   public static BukkitTask playAscendingNote(
      @NotNull Plugin plugin,
      @NotNull final Player player,
      @NotNull final Entity playTo,
      @NotNull final Instrument instrument,
      final int ascendLevel,
      int delay
   ) {
      Objects.requireNonNull(player, "Cannot play note from null player");
      Objects.requireNonNull(playTo, "Cannot play note to null entity");
      if (ascendLevel <= 0) {
         throw new IllegalArgumentException("Note ascend level cannot be lower than 1");
      } else if (ascendLevel > 7) {
         throw new IllegalArgumentException("Note ascend level cannot be greater than 7");
      } else if (delay <= 0) {
         throw new IllegalArgumentException("Delay ticks must be at least 1");
      } else {
         return (new BukkitRunnable() {
            int repeating = ascendLevel;

            public void run() {
               player.playNote(playTo.getLocation(), instrument, Note.natural(1, Tone.values()[ascendLevel - this.repeating]));
               if (this.repeating-- == 0) {
                  this.cancel();
               }
            }
         }).runTaskTimerAsynchronously(plugin, 0L, delay);
      }
   }

   static {
      INSTRUMENT_TO_SOUND.put(Instrument.PIANO, XSound.BLOCK_NOTE_BLOCK_HARP);
      INSTRUMENT_TO_SOUND.put(Instrument.BASS_DRUM, XSound.BLOCK_NOTE_BLOCK_BASEDRUM);
      INSTRUMENT_TO_SOUND.put(Instrument.SNARE_DRUM, XSound.BLOCK_NOTE_BLOCK_SNARE);
      INSTRUMENT_TO_SOUND.put(Instrument.STICKS, XSound.BLOCK_NOTE_BLOCK_HAT);
      INSTRUMENT_TO_SOUND.put(Instrument.BASS_GUITAR, XSound.BLOCK_NOTE_BLOCK_BASS);
      INSTRUMENT_TO_SOUND.put(Instrument.FLUTE, XSound.BLOCK_NOTE_BLOCK_FLUTE);
      INSTRUMENT_TO_SOUND.put(Instrument.BELL, XSound.BLOCK_NOTE_BLOCK_BELL);
      INSTRUMENT_TO_SOUND.put(Instrument.GUITAR, XSound.BLOCK_NOTE_BLOCK_GUITAR);
      INSTRUMENT_TO_SOUND.put(Instrument.CHIME, XSound.BLOCK_NOTE_BLOCK_CHIME);
      INSTRUMENT_TO_SOUND.put(Instrument.XYLOPHONE, XSound.BLOCK_NOTE_BLOCK_XYLOPHONE);
      INSTRUMENT_TO_SOUND.put(Instrument.IRON_XYLOPHONE, XSound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE);
      INSTRUMENT_TO_SOUND.put(Instrument.COW_BELL, XSound.BLOCK_NOTE_BLOCK_COW_BELL);
      INSTRUMENT_TO_SOUND.put(Instrument.DIDGERIDOO, XSound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
      INSTRUMENT_TO_SOUND.put(Instrument.BIT, XSound.BLOCK_NOTE_BLOCK_BIT);
      INSTRUMENT_TO_SOUND.put(Instrument.BANJO, XSound.BLOCK_NOTE_BLOCK_BANJO);
      INSTRUMENT_TO_SOUND.put(Instrument.PLING, XSound.BLOCK_NOTE_BLOCK_PLING);
      INSTRUMENTS.put("HARP", Instrument.PIANO);
      INSTRUMENTS.put("BASEDRUM", Instrument.BASS_DRUM);
      INSTRUMENTS.put("BASE_DRUM", Instrument.BASS_DRUM);
      INSTRUMENTS.put("SNARE", Instrument.SNARE_DRUM);
      INSTRUMENTS.put("BASS", Instrument.BASS_GUITAR);
      INSTRUMENTS.put("COWBELL", Instrument.COW_BELL);

      for (Instrument instrument : Instrument.values()) {
         String name = instrument.name();
         INSTRUMENTS.put(name, instrument);
         StringBuilder alias = new StringBuilder(String.valueOf(name.charAt(0)));
         int index = name.indexOf(95);
         if (index != -1) {
            alias.append(name.charAt(index + 1));
         }

         if (INSTRUMENTS.putIfAbsent(alias.toString(), instrument) != null) {
            for (int i = 0; i < name.length(); i++) {
               char ch = name.charAt(i);
               if (ch == '_') {
                  i++;
               } else {
                  alias.append(ch);
                  if (INSTRUMENTS.putIfAbsent(alias.toString(), instrument) == null) {
                     break;
                  }
               }
            }
         }
      }
   }

   public abstract static class Instruction {
      @Nullable
      public NoteBlockMusic.Sequence parent;
      public int restatement;
      public int restatementFermata;
      public int fermata;

      public Instruction(int restatement, int restatementFermata, int fermata) {
         this.restatement = restatement;
         this.restatementFermata = restatementFermata;
         this.fermata = fermata;
      }

      public abstract void play(Player var1, Supplier<Location> var2, boolean var3);

      public long getEstimatedLength() {
         return (long)this.restatement * this.restatementFermata;
      }
   }

   private static final class InstructionBuilder {
      @NotNull
      final CharSequence script;
      final int len;
      final StringBuilder instrumentBuilder = new StringBuilder(10);
      final StringBuilder pitchBuiler = new StringBuilder(3);
      final StringBuilder volumeBuilder = new StringBuilder(3);
      final StringBuilder restatementBuilder = new StringBuilder(10);
      final StringBuilder restatementDelayBuilder = new StringBuilder(10);
      final StringBuilder fermataBuilder = new StringBuilder(10);
      int i;
      boolean isSequence;
      boolean isBuilding;
      NoteBlockMusic.Sequence sequence = new NoteBlockMusic.Sequence();
      NoteBlockMusic.InstructionParserPhase phase = NoteBlockMusic.InstructionParserPhase.NEUTRAL;
      StringBuilder currentBuilder;

      public InstructionBuilder(@NotNull CharSequence script) {
         this.script = script;

         for (this.len = script.length(); this.i < this.len; this.i++) {
            char ch = script.charAt(this.i);
            switch (ch) {
               case ' ':
                  if (this.isBuilding) {
                     this.isBuilding = false;
                     switch (this.phase) {
                        case NOTE:
                        case RESTATEMENT_DELAY:
                           this.phase = NoteBlockMusic.InstructionParserPhase.FERMATA;
                           this.currentBuilder = this.fermataBuilder;
                        case END_SEQ:
                        case RESTATEMENT:
                        default:
                           break;
                        case FERMATA:
                           this.buildAndAddInstruction();
                           this.prepareHandlers();
                     }
                  }
                  break;
               case '(':
                  NoteBlockMusic.Sequence parent = new NoteBlockMusic.Sequence();
                  parent.parent = this.sequence;
                  this.sequence = parent;
                  break;
               case ')':
                  if (this.sequence.parent == null) {
                     this.err("Cannot find start of the sequence for sequence at: " + this.i);
                  }

                  this.buildAndAddInstruction();
                  this.sequence = this.sequence.parent;
                  this.prepareHandlers();
                  this.phase = NoteBlockMusic.InstructionParserPhase.END_SEQ;
                  this.isSequence = true;
                  break;
               case ',':
                  switch (this.phase) {
                     case INSTRUMENT:
                        this.currentBuilder = this.pitchBuiler;
                        break;
                     case NOTE:
                     case END_SEQ:
                        this.currentBuilder = this.restatementBuilder;
                        break;
                     case RESTATEMENT:
                        this.currentBuilder = this.restatementDelayBuilder;
                        break;
                     default:
                        this.err("Unexpected phase '" + this.phase + "' at index: " + this.i);
                  }

                  this.isBuilding = false;
                  this.phase = this.phase.next();
                  break;
               case ':':
                  if (this.phase == NoteBlockMusic.InstructionParserPhase.NOTE) {
                     this.currentBuilder = this.volumeBuilder;
                  } else {
                     this.err("Unexpected ':' pitch-volume separator at " + this.i + " with current phase: " + this.phase);
                  }
                  break;
               default:
                  if (this.phase == NoteBlockMusic.InstructionParserPhase.NEUTRAL
                     || this.canBuildInstructionInPhase() && NoteBlockMusic.InstructionParserPhase.INSTRUMENT.checkup(ch) != 0) {
                     this.currentBuilder = this.instrumentBuilder;
                     if (this.phase == NoteBlockMusic.InstructionParserPhase.FERMATA) {
                        this.buildAndAddInstruction();
                        this.prepareHandlers();
                     }

                     this.phase = NoteBlockMusic.InstructionParserPhase.INSTRUMENT;
                  }

                  this.isBuilding = true;
                  if ((ch = this.phase.checkup(ch)) == 0) {
                     this.err("Unexpected char at index " + this.i + " with phase " + this.phase + ": " + script.charAt(this.i));
                  }

                  this.currentBuilder.append(ch);
            }
         }

         this.buildAndAddInstruction();
         this.sequence = this.getRoot();
      }

      private NoteBlockMusic.Instruction buildInstruction() {
         int fermata = this.fermataBuilder.length() == 0 ? 0 : Integer.parseInt(this.fermataBuilder.toString());
         int restatement = this.restatementBuilder.length() == 0 ? 1 : Integer.parseInt(this.restatementBuilder.toString());
         int restatementFermata = this.restatementDelayBuilder.length() == 0 ? 0 : Integer.parseInt(this.restatementDelayBuilder.toString());
         NoteBlockMusic.Instruction instruction;
         if (this.isSequence) {
            instruction = new NoteBlockMusic.Sequence(restatement, restatementFermata, fermata);
         } else {
            String instrumentStr = this.instrumentBuilder.toString();
            Instrument instrument = NoteBlockMusic.INSTRUMENTS.get(instrumentStr);
            XSound sound;
            if (instrument == null) {
               sound = XSound.matchXSound(instrumentStr).orElse(null);
            } else {
               sound = NoteBlockMusic.getSoundFromInstrument(instrument);
            }

            String pitchStr = this.pitchBuiler.toString();
            Note note = NoteBlockMusic.parseNote(pitchStr);
            float pitch;
            if (note == null) {
               pitch = Float.parseFloat(pitchStr);
            } else {
               pitch = NoteBlockMusic.noteToPitch(note);
            }

            float volume = 5.0F;
            if (this.volumeBuilder.length() != 0) {
               volume = Float.parseFloat(this.volumeBuilder.toString());
            }

            instruction = new NoteBlockMusic.Sound(sound, pitch, volume, restatement, restatementFermata, fermata);
         }

         return instruction;
      }

      private void prepareHandlers() {
         this.instrumentBuilder.setLength(0);
         this.pitchBuiler.setLength(0);
         this.volumeBuilder.setLength(0);
         this.restatementBuilder.setLength(0);
         this.restatementDelayBuilder.setLength(0);
         this.fermataBuilder.setLength(0);
         this.phase = NoteBlockMusic.InstructionParserPhase.NEUTRAL;
         this.isBuilding = false;
         this.isSequence = false;
      }

      private boolean canBuildInstructionInPhase() {
         switch (this.phase) {
            case RESTATEMENT:
            case RESTATEMENT_DELAY:
            case FERMATA:
               return true;
            default:
               return false;
         }
      }

      private void buildAndAddInstruction() {
         this.sequence.addInstruction(this.buildInstruction());
      }

      private NoteBlockMusic.Sequence getRoot() {
         NoteBlockMusic.Sequence sequence = this.sequence;

         while (sequence.parent != null) {
            sequence = sequence.parent;
         }

         return sequence;
      }

      private String illustrateError() {
         return '\n' + this.script.toString() + '\n' + Strings.repeat(" ", this.i) + '^';
      }

      private void err(String str) {
         throw new IllegalStateException(str + this.illustrateError());
      }
   }

   private enum InstructionParserPhase {
      NEUTRAL {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return INSTRUMENT;
         }

         @Override
         protected char checkup(char ch) {
            throw new AssertionError("Checkup should not be performed on NEUTRAL instruction parser phase");
         }
      },
      INSTRUMENT {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return NOTE;
         }

         @Override
         protected char checkup(char ch) {
            if (ch >= 'a' && ch <= 'z') {
               return (char)(ch & 95);
            } else {
               return (ch < 'A' || ch > 'Z') && ch != '_' && ch != '-' ? '\u0000' : ch;
            }
         }
      },
      NOTE {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return RESTATEMENT;
         }

         @Override
         protected char checkup(char ch) {
            if (ch >= 'a' && ch <= 'z') {
               return (char)(ch & 95);
            } else {
               return (ch < 'A' || ch > 'Z') && !NoteBlockMusic.isDigit(ch) && ch != '.' && ch != '_' && ch != '#' ? '\u0000' : ch;
            }
         }
      },
      END_SEQ {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return RESTATEMENT;
         }

         @Override
         protected char checkup(char ch) {
            return '\u0000';
         }
      },
      RESTATEMENT {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return RESTATEMENT_DELAY;
         }

         @Override
         protected char checkup(char ch) {
            return NoteBlockMusic.isDigit(ch) ? ch : '\u0000';
         }
      },
      RESTATEMENT_DELAY {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return FERMATA;
         }

         @Override
         protected char checkup(char ch) {
            return NoteBlockMusic.isDigit(ch) ? ch : '\u0000';
         }
      },
      FERMATA {
         @Override
         protected NoteBlockMusic.InstructionParserPhase next() {
            return NEUTRAL;
         }

         @Override
         protected char checkup(char ch) {
            return NoteBlockMusic.isDigit(ch) ? ch : '\u0000';
         }
      };

      InstructionParserPhase() {
      }

      protected abstract NoteBlockMusic.InstructionParserPhase next();

      protected abstract char checkup(char var1);
   }

   public static class Sequence extends NoteBlockMusic.Instruction {
      public Collection<NoteBlockMusic.Instruction> instructions = new ArrayList<>(16);

      public Sequence() {
         super(1, 0, 0);
      }

      public Sequence(NoteBlockMusic.Instruction first) {
         super(1, 0, 0);
         this.instructions.add(first);
      }

      public Sequence(int restatement, int restatementFermata, int fermata) {
         super(restatement, restatementFermata, fermata);
      }

      @Override
      public void play(Player player, Supplier<Location> location, boolean playAtLocation) {
         for (int repeat = this.restatement; repeat > 0; repeat--) {
            for (NoteBlockMusic.Instruction instruction : this.instructions) {
               instruction.play(player, location, playAtLocation);
            }

            if (this.restatementFermata > 0) {
               NoteBlockMusic.sleep(this.restatementFermata);
            }
         }

         if (this.fermata > 0) {
            NoteBlockMusic.sleep(this.fermata);
         }
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder(200 + this.instructions.size() * 100);
         builder.append("Sequence:{restatement=")
            .append(this.restatement)
            .append(", restatementFermata=")
            .append(this.restatementFermata)
            .append(", fermata=")
            .append(this.fermata)
            .append(", instructions[");
         int i = 0;
         int size = this.instructions.size();

         for (NoteBlockMusic.Instruction instruction : this.instructions) {
            builder.append(instruction);
            if (++i < size) {
               builder.append(", ");
            }
         }

         builder.append("]}");
         return builder.toString();
      }

      public void addInstruction(NoteBlockMusic.Instruction instruction) {
         instruction.parent = this;
         this.instructions.add(instruction);
      }

      @Override
      public long getEstimatedLength() {
         long result = (long)this.restatement * this.restatementFermata;

         for (NoteBlockMusic.Instruction instruction : this.instructions) {
            result += instruction.getEstimatedLength();
         }

         return result;
      }
   }

   public static class Sound extends NoteBlockMusic.Instruction {
      public XSound sound;
      public float volume;
      public float pitch;

      public Sound(Instrument instrument, Note note, float volume, int restatement, int restatementFermata, int fermata) {
         super(restatement, restatementFermata, fermata);
         this.sound = NoteBlockMusic.getSoundFromInstrument(instrument);
         this.pitch = NoteBlockMusic.noteToPitch(note);
         this.volume = volume;
      }

      public Sound(XSound sound, float pitch, float volume, int restatement, int restatementFermata, int fermata) {
         super(restatement, restatementFermata, fermata);
         this.sound = sound;
         this.pitch = pitch;
         this.volume = volume;
      }

      public void setSound(Instrument instrument) {
         this.sound = NoteBlockMusic.getSoundFromInstrument(instrument);
      }

      public void setPitch(Note note) {
         this.pitch = NoteBlockMusic.noteToPitch(note);
      }

      @Override
      public void play(Player player, Supplier<Location> location, boolean playAtLocation) {
         org.bukkit.Sound bukkitSound = this.sound.get();

         for (int repeat = this.restatement; repeat > 0; repeat--) {
            Location finalLocation = location.get();
            if (bukkitSound != null) {
               if (playAtLocation) {
                  finalLocation.getWorld().playSound(finalLocation, bukkitSound, this.volume, this.pitch);
               } else {
                  player.playSound(finalLocation, bukkitSound, this.volume, this.pitch);
               }
            }

            if (this.restatementFermata > 0) {
               NoteBlockMusic.sleep(this.restatementFermata);
            }
         }

         if (this.fermata > 0) {
            NoteBlockMusic.sleep(this.fermata);
         }
      }

      @Override
      public String toString() {
         return "Sound:{sound="
            + this.sound
            + ", pitch="
            + this.pitch
            + ", volume="
            + this.volume
            + ", restatement="
            + this.restatement
            + ", restatementFermata="
            + this.restatementFermata
            + ", fermata="
            + this.fermata
            + '}';
      }
   }
}
