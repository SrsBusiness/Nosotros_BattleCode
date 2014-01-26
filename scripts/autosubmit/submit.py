import os
import subprocess
import requests
import re
from getpass import getpass

if __name__ == "__main__":
    s = requests.session()
    print "Battlecode.org login"
    uname = raw_input("Username: ")
    pword = getpass("Password: ")
    result = s.post("https://www.battlecode.org/contestants/login/",
           {'username': uname,
            'password': pword}).content
    if re.findall("Welcome back", result) < 1:
        print "Failed logging in."
        exit()
    projectDir = ''
    if "RobotPlayer.java" in os.listdir('.'):
        projectDir = '.'
    elif "RobotPlayer.java" in os.listdir(os.environ['HOME']+'/battlecode/teams/Nosotros_BattleCode/team090'):
        projectDir = os.environ['HOME']+'/battlecode/teams/Nosotros_BattleCode/team090'
    else:
        projectDir = raw_input("Could not autodetect team to submit.\nSpecify explicit path: ")
    print "Generating jar...\r",
    p = subprocess.Popen(["jar","-cf", "/tmp/battlecode.jar", projectDir+"/"],
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)
    output, errors = p.communicate()
    print "Submitting jarfile...\r",
    p = subprocess.Popen(["git","log", "-1"],
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE)
    git_id, errors = p.communicate()
    git_id = re.findall("commit (.*)", git_id)[0][:16]
    s.post("https://www.battlecode.org/contestants/upload/",
           {'label': git_id},
           files={'filename': open('/tmp/battlecode.jar', 'rb')})
    print "Jarfile submitted as %s." % git_id
