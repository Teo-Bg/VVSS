package drinkshop.repository.file;

import drinkshop.repository.AbstractRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class FileAbstractRepository<ID, E>
        extends AbstractRepository<ID, E> {

    private static final String UTF8_BOM = "\uFEFF";

    protected String fileName;

    protected FileAbstractRepository(String fileName) {
        this.fileName = fileName;
        //loadFromFile();
    }

    protected void loadFromFile() {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                // Strip UTF-8 BOM if present on the first line
                if (firstLine) {
                    if (line.startsWith(UTF8_BOM)) {
                        line = line.substring(1);
                    }
                    firstLine = false;
                }
                if (line.isBlank()) continue;   // skip empty / whitespace-only lines
                E entity = extractEntity(line);
                super.save(entity);
            }

        } catch (IOException e) {
            throw new RuntimeException("Nu s-a putut citi fișierul: " + fileName, e);
        }
    }

    private void writeToFile() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {

            for (E entity : entities.values()) {
                bw.write(createEntityAsString(entity));
                bw.newLine();
            }

        } catch (IOException e) {
            throw new RuntimeException("Nu s-a putut scrie fișierul: " + fileName, e);
        }
    }

    @Override
    public E save(E entity) {
        E e = super.save(entity);
        writeToFile();
        return e;
    }

    @Override
    public E delete(ID id) {
        E e = super.delete(id);
        writeToFile();
        return e;
    }

    protected abstract E extractEntity(String line);

    protected abstract String createEntityAsString(E entity);
}
