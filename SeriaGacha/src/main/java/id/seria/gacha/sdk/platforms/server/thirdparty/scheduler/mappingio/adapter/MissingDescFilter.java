package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.adapter;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.mappingio.MappingVisitor;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

public final class MissingDescFilter extends ForwardingMappingVisitor {
   public MissingDescFilter(MappingVisitor next) {
      super(next);
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      return srcDesc == null ? false : super.visitField(srcName, srcDesc);
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      return srcDesc == null ? false : super.visitMethod(srcName, srcDesc);
   }
}
