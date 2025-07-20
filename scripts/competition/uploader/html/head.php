<?php
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}
if (isset($_GET['logout'])) {
    session_destroy();
    header("Location: " . strtok($_SERVER["REQUEST_URI"], '?'));
    exit;
}
?>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Rescue Simulation dashboard</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container-fluid">
    <a class="navbar-brand" href="#">Rescue Simulation Dashboard</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
            data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false"
            aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    
    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav me-auto">
        <li class="nav-item">
          <a class="nav-link active" href="/">Team Area</a>
        </li>
 <li class="nav-item">
          <a class="nav-link" href="/live.php">Live</a>
        </li>
 <li class="nav-item">
          <a class="nav-link" href="/results/">Results</a>
        </li>
        <!-- You can add more links here -->
      </ul>
      <?php if ($_SESSION['logged_in']){?>
      <span class="navbar-text text-light me-3">
        👤 <?= htmlspecialchars($_SESSION['logged_in'] ?? '') ?>
      </span>
      <a href="?logout=1" class="btn btn-outline-light btn-sm">Logout</a>
      <?}?>
    </div>
  </div>
</nav>
