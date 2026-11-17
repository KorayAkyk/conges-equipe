"""Additionne les rapports Surefire et ecrit le nombre de tests dans le resume du job GitHub."""
import glob
import os
import sys
import xml.etree.ElementTree as ET

totaux = {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
for chemin in glob.glob(os.path.join(sys.argv[1], "TEST-*.xml")):
    racine = ET.parse(chemin).getroot()
    for cle in totaux:
        totaux[cle] += int(racine.get(cle, 0))

ligne = (f"Backend : {totaux['tests']} tests, {totaux['failures']} echec(s), "
         f"{totaux['errors']} erreur(s), {totaux['skipped']} ignore(s)")
print(ligne)
resume = os.environ.get("GITHUB_STEP_SUMMARY")
if resume:
    with open(resume, "a", encoding="utf-8") as f:
        f.write(ligne + "\n")
