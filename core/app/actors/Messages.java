package actors;

import edu.illinois.cs.cogcomp.core.experiments.ClassificationTester;
import models.Job;

public class Messages {
    public static class SetUpJobMessage {
        private String conf_id;
        private String url;
        private String record_id;

        public SetUpJobMessage(String conf_id, String url, String record_id) {
            super();
            this.conf_id = conf_id;
            this.url = url;
            this.record_id = record_id;
        }

        public String getConf_id() {
            return conf_id;
        }

        public String getUrl() {
            return url;
        }

        public String getRecord_id() {
            return record_id;
        }
    }

    public static class StartJobMessage {
        private Job job;

        public StartJobMessage(Job job) {
            super();
            this.job = job;
        }

        public Job getJob() {
            return job;
        }
    }

    public static class StatusUpdate {
        private int completed;
        private int skipped;
        private int total;
        private String record_id;
        private String error;
        private ClassificationTester eval;

        public StatusUpdate(int completed, int skipped, int total, String record_id, String error, ClassificationTester eval) {
            this.completed = completed;
            this.skipped = skipped;
            this.total = total;
            this.record_id = record_id;
            this.error = error;
            this.eval = eval;
        }

        public int getTotal() {
            return total;
        }

        public int getCompleted() {
            return completed;
        }

        public int getSkipped() {
            return skipped;
        }
        
        public String getRecord_id() {
            return record_id;
        }

        public String getError() {
            return error;
        }

        public ClassificationTester getEvaluation() { return eval; }
    }

    public static class StatusRequest {
        private String record_id;

        public StatusRequest(String record_id) {
            this.record_id = record_id;
        }

        public String getRecord_id() {
            return record_id;
        }
    }
}
