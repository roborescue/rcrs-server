<?php
session_start();

// Users
$users = [
    'AIT' => 'asd',
    '3RA' => 'asdasd',
    'TIM' => 'sadasd',
    'admin' => 'asd',
];

// Handle login
$loginError = '';
if (!isset($_SESSION['logged_in'])) {
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['username'], $_POST['password'])) {
        if (isset($users[$_POST['username']]) && $users[$_POST['username']] === $_POST['password']) {
            $_SESSION['logged_in'] = $_POST['username'];
        } else {
            $loginError = "Invalid username or password.";
        }
    }

    if (!isset($_SESSION['logged_in'])) {
include("head.php");
?>            <div class="container mt-5" style="max-width: 400px;">
                <h3 class="mb-3">Login</h3>
                <?php if ($loginError): ?>
                    <div class="alert alert-danger"><?= htmlspecialchars($loginError) ?></div>
                <?php endif; ?>
                <form method="POST">
                    <div class="mb-3">
                        <label class="form-label">Username</label>
                        <input name="username" type="text" class="form-control">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Password</label>
                        <input name="password" type="password" class="form-control">
                    </div>
                    <button type="submit" class="btn btn-primary">Login</button>
                </form>
            </div>
<?php
include("foot.php");
exit;
    }

}


