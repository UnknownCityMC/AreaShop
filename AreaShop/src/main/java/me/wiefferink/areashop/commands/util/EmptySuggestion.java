package me.wiefferink.areashop.commands.util;

import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class EmptySuggestion {

    public static <C> SuggestionProvider<C> create() {
         return (context, input) ->  CompletableFuture.completedFuture(Collections.emptyList());
    }
}
