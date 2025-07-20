<?php
include("login_admin.php");
include("head.php");

?>




<style>
  .iframe-wrapper {
    position: relative;
    border: 1px solid #ccc;
    border-radius: 8px;
    margin-bottom: 20px;
    cursor: pointer;
  }

  .iframe-wrapper iframe {
    width: 100%;
    height: 45vh;
  }

  .iframe-link {}
</style>
<div class="container-fluid mt-4">
  <h3 class="mb-4">Rescue Sim Runner</h3>
  <div class="row g-10">
    <div class="col-12">
      <div class="iframe-wrapper">
        <iframe id="iframe0" allow="fullscreen" allowfullscreen referrerpolicy="no-referrer"
          src="http://186.237.58.28:8082/vnc.html?autoconnect=true&resize=scale&password=PASSWORD"
          title="Runner"></iframe>
        <a class="btn btn-primary" onclick="openFullscreen('iframe0')">Full Screen</a>
        <a class="btn btn-secondary"
          href="http://186.237.58.28:8082/vnc.html?autoconnect=true&resize=scale&password=PASSWORD" target="_blank"
          rel="noopener noreferrer">Open in new tab</a>
      </div>
    </div>
  </div>
</div>
<div class="row g-10">
  <div class="col-12 col-md-4">
    <div class="iframe-wrapper">
      <iframe id="iframe1" allow="fullscreen" allowfullscreen referrerpolicy="no-referrer"
        src="http://186.237.58.28:8080/vnc.html?autoconnect=true&resize=scale&password=PASSWORD"
        title="Kernel 1"></iframe>
      <a class="btn btn-primary" onclick="openFullscreen('iframe1')">Full Screen</a>
      <a class="btn btn-secondary"
        href="http://186.237.58.28:8080/vnc.html?autoconnect=true&resize=scale&password=PASSWORD" target="_blank"
        rel="noopener noreferrer">Open in new tab</a>
    </div>
  </div>
  <div class="col-12 col-md-4">
    <div class="iframe-wrapper">
      <iframe id="iframe2" allow="fullscreen" allowfullscreen referrerpolicy="no-referrer"
        src="http://186.237.58.14:8080/vnc.html?autoconnect=true&resize=scale&password=PASSWORD"
        title="Kernel 2"></iframe>
      <a class="btn btn-primary" onclick="openFullscreen('iframe2')">Full Screen</a>
      <a class="btn btn-secondary"
        href="http://186.237.58.14:8080/vnc.html?autoconnect=true&resize=scale&password=PASSWORD" target="_blank"
        rel="noopener noreferrer">Open in new tab</a>
    </div>
  </div>

  <div class="col-12 col-md-4">
    <div class="iframe-wrapper">
      <iframe id="iframe3" allow="fullscreen" allowfullscreen referrerpolicy="no-referrer" src="status.php">
      </iframe>
      <a class="btn btn-secondary" target="_blank" href="status.php">Open in new tab</a>
    </div>
  </div>

</div>
</div>
<script>
  function openFullscreen(elemid) {
    const elem = document.getElementById(elemid)
    if (elem.requestFullscreen) {
      elem.requestFullscreen();
    } else if (elem.webkitRequestFullscreen) { /* Safari */
      elem.webkitRequestFullscreen();
    } else if (elem.msRequestFullscreen) { /* IE11 */
      elem.msRequestFullscreen();
    }
  }
</script>

<?php
include("foot.php");
?>