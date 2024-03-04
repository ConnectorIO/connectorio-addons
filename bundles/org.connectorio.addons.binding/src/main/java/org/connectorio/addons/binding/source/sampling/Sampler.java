package org.connectorio.addons.binding.source.sampling;

import java.util.concurrent.CompletableFuture;

public interface Sampler {

  CompletableFuture<?> fetch();

}
