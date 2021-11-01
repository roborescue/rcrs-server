echo """
from christopher.models import *
Match.objects.all().delete()

print('Delete all match')
""" | /home/rescue/web-viewer/rcrs-web-viewer-server/.venv/bin/python /home/rescue/web-viewer/rcrs-web-viewer-server/manage.py shell
rm /home/rescue/web-viewer/rcrs-web-viewer-server/prepared_logs/*
