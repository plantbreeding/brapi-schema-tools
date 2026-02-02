package org.brapi.schematools.core.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderOptions;
import org.brapi.schematools.core.validiation.Validation;

/**
 * Abstract class for all Generator Options
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractMainGeneratorOptions extends AbstractGeneratorOptions {

    private BrAPISchemaReaderOptions brAPISchemaReader ;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(brAPISchemaReader, "'brAPISchemaReader' options on %s is null", this.getClass().getSimpleName())
            .merge(brAPISchemaReader);
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public AbstractMainGeneratorOptions override(AbstractMainGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.brAPISchemaReader != null) {
            brAPISchemaReader.override(overrideOptions.brAPISchemaReader);
        }

        return this;
    }

    protected static void loadBrAPISchemaReaderOptions(AbstractMainGeneratorOptions options) {
        if (options.getBrAPISchemaReader() == null) {
            options.setBrAPISchemaReader(BrAPISchemaReaderOptions.load());
        } else {
            options.setBrAPISchemaReader(
                BrAPISchemaReaderOptions.load().override(options.getBrAPISchemaReader())
            ) ;
        }
    }
}
