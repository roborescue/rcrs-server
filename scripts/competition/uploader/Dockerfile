FROM php:8.2-apache

# Enable Apache mod_rewrite (optional, not required for this script)
RUN a2enmod rewrite autoindex dir

# Enable directory browsing on /var/www/html
RUN echo '<VirtualHost *:80>\n\
    DocumentRoot /var/www/html\n\
    <Directory /var/www/html>\n\
        Options +Indexes\n\
        AllowOverride All\n\
        Require all granted\n\
    </Directory>\n\
</VirtualHost>' > /etc/apache2/sites-available/000-default.conf
# Create /opt/rescue and set permissions
RUN mkdir -p /opt/rescue && chmod +x /opt/rescue

# Copy and mark compile.sh executable (optional if using volume instead)
# COPY compile.sh /opt/rescue/compile.sh
# RUN chmod +x /opt/rescue/compile.sh
RUN echo "upload_max_filesize = 100M\npost_max_size = 100M" > /usr/local/etc/php/conf.d/uploads.ini
# Recommended: install unzip and tar for handling uploads
RUN apt-get update && apt-get install -y ssh openjdk-17-jdk unzip tar  libzip-dev zip && rm -rf /var/lib/apt/lists/*
