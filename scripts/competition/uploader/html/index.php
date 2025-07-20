<?php
include("login.php");
include("head.php");

$feedback = '';
$compileOutput = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['archive'])) {
    if (!isset($_FILES['archive']) || $_FILES['archive']['error'] !== UPLOAD_ERR_OK) {
        $feedback = '<div class="alert alert-danger">❌ Upload error. Please try again.</div>';
    } else {
        $uploadedFile = $_FILES['archive']['tmp_name'];
        $originalName = $_FILES['archive']['name'];
        $extension = strtolower(pathinfo($originalName, PATHINFO_EXTENSION));

        if (!file_exists($uploadedFile)) {
            $feedback = '<div class="alert alert-danger">❌ Uploaded file not found.</div>';
        } else {
            $tempDir = sys_get_temp_dir() . '/rescue_' . uniqid();
            mkdir($tempDir, 0700, true);
            $extractSuccess = false;

            if ($extension === 'zip') {
                $cmd = "unzip -q " . escapeshellarg($uploadedFile) . " -d " . escapeshellarg($tempDir);
                exec($cmd, $out, $ret);
                $extractSuccess = $ret === 0;
            } elseif (in_array($extension, ['tgz', 'gz', 'tar'])) {
                $cmd = "tar -xzf " . escapeshellarg($uploadedFile) . " -C " . escapeshellarg($tempDir);
                exec($cmd, $out, $ret);
                $extractSuccess = $ret === 0;
            }

            if (!$extractSuccess) {
                $feedback = '<div class="alert alert-danger">❌ Extraction failed. Make sure it\'s a valid archive.</div>';
            } else {
                    $compileScript = '/opt/compile/compile.sh';
                    if (!is_executable($compileScript)) {
                        $feedback = "<div class='alert alert-danger'>❌ Compile script not found or not executable: $compileScript</div>";
                        rrmdir($tempDir);
                    } else {
                        $output = [];
                        exec(escapeshellcmd($compileScript) . " " . $_SESSION['logged_in'] . " "  . escapeshellarg($tempDir) . " 2>&1", $output, $status);
                        $compileOutput = "<pre class='bg-light border p-2'>" . htmlspecialchars(implode("\n", $output)) . "</pre>";
                        $feedback = '<div class="alert alert-success">✅ Compile finished.</div>';
                        rrmdir($tempDir);
                    }
            }
        }
    }
}

// Render main page
showPage(function () use ($feedback, $compileOutput) {
    ?>
    <div class="container mt-4">
        <div class="d-flex justify-content-between align-items-center">
            <h3>Upload & Compile</h3>
            <a href="?logout=1" class="btn btn-outline-danger btn-sm">Logout <?php echo $_SESSION['logged_in'];?></a>
        </div>
        <div class="mt-3">
            <div class="card">
                <div class="card-body">
                    <h5>Upload Instructions</h5>
                    <ul>
                        <li>Accepted file formats: <code>.zip</code>, <code>.tgz</code></li>
                        <li>Must contain <code>config/</code> and <code>src/</code> folders</li>
                        <li>Compiles using  <code>https://github.com/roborescue/adf-sample-agent-java</code></li>
                    </ul>
                </div>
            </div>
        </div>

        <?= $feedback ?>

        <form method="POST" enctype="multipart/form-data" class="mt-3">
            <div class="mb-3">
                <label class="form-label">Select archive file</label>
                <input type="file" name="archive" class="form-control" accept=".zip,.tgz,.tar.gz" required>
            </div>
            <button type="submit" class="btn btn-primary">Upload & Compile</button>
        </form>

        <?= $compileOutput ?>
    </div>
    <?php
});

function showPage($contentFn){
    ?>
    <?php $contentFn(); ?>
    <?php
}

function rrmdir($dir)
{
    if (!is_dir($dir)) return;
    foreach (scandir($dir) as $file) {
        if ($file === '.' || $file === '..') continue;
        $path = "$dir/$file";
        is_dir($path) ? rrmdir($path) : unlink($path);
    }
    rmdir($dir);
}

include("foot.php");
