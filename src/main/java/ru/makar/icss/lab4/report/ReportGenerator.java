package ru.makar.icss.lab4.report;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import ru.makar.icss.lab4.model.Info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReportGenerator {
    private static final String PATH_REPORT_TEMPLATE = "template/report-template.twig";

    private String targetName;
    private Info groupsInfo;
    private JtwigTemplate template;

    public ReportGenerator() {
        EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder
                .configuration()
                .resources()
                .withDefaultInputCharset(Charset.forName("UTF-8"))
                .and()
                .build();
        template = JtwigTemplate.classpathTemplate(PATH_REPORT_TEMPLATE, configuration);
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setGroupsInfo(Info groupsInfo) {
        this.groupsInfo = groupsInfo;
    }

    public File generate(File destinationPath) {
        if (destinationPath != null && destinationPath.exists() && destinationPath.isDirectory()) {
            File report = new File(destinationPath, isNameEmpty() ? "report.html" : targetName);
            if (report.exists()) {
                if (!report.delete()) {
                    return null;
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(report.toURI()))) {
                writer.write(generateReportContent());
                return report;
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    private boolean isNameEmpty() {
        return targetName == null || targetName.isEmpty();
    }

    private String generateReportContent() {
        JtwigModel model = JtwigModel.newModel()
                .with("groups", groupsInfo.getGroups().getGroup())
                .with("students", groupsInfo.getStudents().getStudent());
        return template.render(model);
    }
}
