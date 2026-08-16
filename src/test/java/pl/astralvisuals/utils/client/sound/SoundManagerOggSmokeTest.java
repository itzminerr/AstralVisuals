package pl.astralvisuals.utils.client.sound;

import java.io.InputStream;

/** Regression check for the native crash caused by manually freeing JVM-owned OGG buffers. */
public final class SoundManagerOggSmokeTest {
   private static final String SOUND = "/assets/minecraft/hitsounds/abmiss.ogg";
   private static final int ITERATIONS = 250;

   private SoundManagerOggSmokeTest() {
   }

   public static void main(String[] args) throws Exception {
      int expectedLength = -1;
      for (int iteration = 0; iteration < ITERATIONS; iteration++) {
         try (InputStream input = SoundManagerOggSmokeTest.class.getResourceAsStream(SOUND)) {
            require(input != null, "Missing test sound: " + SOUND);
            SoundManager.DecodedAudio audio = SoundManager.decodeOgg(input);
            require(audio.format() != null, "OGG decoder returned no audio format");
            require(audio.pcm().length > 0, "OGG decoder returned empty PCM data");
            if (expectedLength < 0) {
               expectedLength = audio.pcm().length;
            } else {
               require(audio.pcm().length == expectedLength, "PCM length changed between decoder runs");
            }
         }

         if ((iteration + 1) % 25 == 0) {
            System.gc();
         }
      }

      System.out.println("OGG SMOKE TEST PASSED: " + ITERATIONS + " decodes, " + expectedLength + " PCM bytes each");
   }

   private static void require(boolean condition, String message) {
      if (!condition) {
         throw new AssertionError(message);
      }
   }
}
