package pl.astralvisuals.utils.display.font.entry;

import pl.astralvisuals.utils.display.font.glyph.Glyph;

public record DrawEntry(float atX, float atY, int color, Glyph toDraw) {
}
