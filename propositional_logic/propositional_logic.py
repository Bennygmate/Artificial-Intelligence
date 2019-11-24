#!/usr/bin/python
# Solves Propositional Logic based problems
import sys
import re
proofs=[]

def main():
	sequent = sys.argv[1:]
	sequent = ' '.join(map(str, sequent)) # Just in case
	left_side, right_side = sequentSplit(sequent)
	rule = applyRule(left_side, right_side)
	# Add last (working backwards)
	proofAdd(sequent, rule)
	next_sequent = ruleHandler(rule, left_side, right_side)
	if (re.match("{.*seq.*} and {.*seq.*}", next_sequent)):
		proofAdd(next_sequent, rule)
	if(sequentProof(next_sequent) == True):
		print "True"
	else:
		print "False"
	for proof in reversed(proofs):
		print proof

# Return true if sequent matches, else false
# In case of split into two sequents it will return false
# In the case that one sequent doesn't hold
# For representing two sequents, 
# it was done through {[] seq []} and {[] seq []}
def sequentProof(next_sequent):
	while(next_sequent != False):
		if (next_sequent == "NO RULE"):
			return False
		if (not re.match("{.*seq.*} and {.*seq.*}", next_sequent)):
			left_side, right_side = sequentSplit(next_sequent)
			rule = applyRule(left_side, right_side)
			proofAdd(next_sequent, rule)
			if (rule == "Rule P1"):
				return True
			next_sequent = ruleHandler(rule, left_side, right_side)
		if (re.match("{.*seq.*} and {.*seq.*}", next_sequent)):
			split_sequent = re.match("{(.*)} and {(.*)}", next_sequent)
			first_sequent = split_sequent.group(1)
			second_sequent = split_sequent.group(2)
			prev = proofs[-1]
			prev_seq = re.match("(.*) \[Rule.*", prev)
			if (not re.match("{.*seq.*} and {.*seq.*}", prev_seq.group(1))):
				left_side, right_side = sequentSplit(prev_seq.group(1))
				rule = applyRule(left_side, right_side)
				proofAdd(next_sequent, rule)
			firstseq = sequentProof(first_sequent)
			if (firstseq):
				secondseq = sequentProof(second_sequent)
				if(secondseq):
					return True
				else:
					return False
			else:
				return False

# Function to apply the rule on the sides of sequent
# Returns the new sequent
def ruleHandler(rule, left_side, right_side):
	new_sequent = ""
	left_atoms = re.split(r', ', left_side)
	right_atoms = re.split(r', ', right_side)
	if (rule == "Rule P1"):
		return True
	if (rule == "Rule P2a"):
		for atom in right_atoms:
			if (re.match('neg\(.*\)', atom)):
				negated = atom
				negated = re.sub('^neg\(', '', negated)
				negated = re.sub('\)$', '', negated)
				left_atoms.append(negated)
				right_atoms.remove(atom)
			if (re.match('\(neg \w+\)', atom)):
				single_atom = re.match('\(neg (\w+)\)', atom)
				single = single_atom.group(1)
				left_atoms.append(single)
				right_atoms.remove(atom)
	if (rule == "Rule P2b"):
		for atom in left_atoms:
			if (re.match("neg\(.*\)", atom)):
				negated = atom
				negated = re.sub('^neg\(', '', negated)
				negated = re.sub('\)$', '', negated)
				right_atoms.append(negated)
				left_atoms.remove(atom)
			if (re.match('\(neg \w+\)', atom)):
				single_atom = re.match('\(neg (\w+)\)', atom)
				single = single_atom.group(1)
				right_atoms.append(single)
				left_atoms.remove(atom)
	if (rule == "Rule P3a"):
		for atom in right_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ and \w+$", atom)):
				side = re.match("^(\w+) and (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ and \(.+\)$", atom)):
				side = re.match("^(\w+) and \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) and \w+$", atom)):
				side = re.match("^\((.*)\) and (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) and \(.+\)$", atom)):
				side = re.match("^\((.+)\) and \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				right_atoms.remove(atom)
				first_left = process(left_atoms)
				first_right = insertTmpAtom(left_atom, right_atoms)
				second_left = process(left_atoms)
				second_right = insertTmpAtom(right_atom, right_atoms)
				first_seq = "{[" + first_left + "]" + " seq " + "[" + first_right + "]}"
				second_seq = "{[" + second_left + "]" + " seq " + "[" + second_right + "]}"
				new_seq = first_seq + " and " + second_seq
				return new_seq
				
	if (rule == "Rule P3b"):
		for atom in left_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ and \w+$", atom)):
				side = re.match("^(\w+) and (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ and \(.+\)$", atom)):
				side = re.match("^(\w+) and \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) and \w+$", atom)):
				side = re.match("^\((.*)\) and (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) and \(.+\)$", atom)):
				side = re.match("^\((.+)\) and \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				left_atoms.remove(atom)
				left_atoms.append(left_atom)
				left_atoms.append(right_atom)
				# Process
				left_side = process(left_atoms)
				right_side = process(right_atoms)
				new_sequent = "[" + left_side + "]" + " seq " + "[" + right_side + "]"
				return new_sequent
	if (rule == "Rule P4a"):
		for atom in right_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ or \w+$", atom)):
				side = re.match("^(\w+) or (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ or \(.+\)$", atom)):
				side = re.match("^(\w+) or \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) or \w+$", atom)):
				side = re.match("^\((.*)\) or (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) or \(.+\)$", atom)):
				side = re.match("^\((.+)\) or \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				right_atoms.remove(atom)
				right_atoms.append(left_atom)
				right_atoms.append(right_atom)
				# Process
				left_side = process(left_atoms)
				right_side = process(right_atoms)
				new_sequent = "[" + left_side + "]" + " seq " + "[" + right_side + "]"
				return new_sequent

	if (rule == "Rule P4b"):
		for atom in left_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ or \w+$", atom)):
				side = re.match("^(\w+) or (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ or \(.+\)$", atom)):
				side = re.match("^(\w+) or \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) or \w+$", atom)):
				side = re.match("^\((.*)\) or (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) or \(.+\)$", atom)):
				side = re.match("^\((.+)\) or \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				left_atoms.remove(atom)
				first_left = insertTmpAtom(left_atom, left_atoms)
				second_left = insertTmpAtom(right_atom, left_atoms)
				right_side = process(right_atoms)
				first_seq = "{[" + first_left + "]" + " seq " + "[" + right_side + "]}"
				second_seq = "{[" + second_left + "]" + " seq " + "[" + right_side + "]}"
				new_seq = first_seq + " and " + second_seq
				return new_seq
	if (rule == "Rule P5a"):
		for atom in right_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ imp \w+$", atom)):
				side = re.match("^(\w+) imp (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ imp \(.+\)$", atom)):
				side = re.match("^(\w+) imp \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) imp \w+$", atom)):
				side = re.match("^\((.*)\) imp (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) imp \(.+\)$", atom)):
				side = re.match("^\((.+)\) imp \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				right_atoms.remove(atom)
				left_atoms.append(left_atom)
				right_atoms.append(right_atom)
				# Process
				left_side = process(left_atoms)
				right_side = process(right_atoms)
				new_sequent = "[" + left_side + "]" + " seq " + "[" + right_side + "]"
				return new_sequent
	if (rule == "Rule P5b"):
		for atom in left_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ imp \w+$", atom)):
				side = re.match("^(\w+) imp (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ imp \(.+\)$", atom)):
				side = re.match("^(\w+) imp \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) imp \w+$", atom)):
				side = re.match("^\((.*)\) imp (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) imp \(.+\)$", atom)):
				side = re.match("^\((.+)\) imp \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				left_atoms.remove(atom)
				first_left = insertTmpAtom(right_atom, left_atoms)
				second_left = process(left_atoms)
				second_right = insertTmpAtom(left_atom, right_atoms)
				first_seq = "{[" + first_left + "]" + " seq " + "[" + right_side + "]}"
				second_seq = "{[" + second_left + "]" + " seq " + "[" + second_right + "]}"
				new_seq = first_seq + " and " + second_seq
				return new_seq
	if (rule == "Rule P6a"):
		for atom in right_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ iff \w+$", atom)):
				side = re.match("^(\w+) iff (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ iff \(.+\)$", atom)):
				side = re.match("^(\w+) iff \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) iff \w+$", atom)):
				side = re.match("^\((.*)\) iff (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) iff \(.+\)$", atom)):
				side = re.match("^\((.+)\) iff \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				right_atoms.remove(atom)
				first_left = insertTmpAtom(left_atom, left_atoms)
				first_right = insertTmpAtom(right_atom, right_atoms)
				second_left = insertTmpAtom(right_atom, left_atoms)
				second_right = insertTmpAtom(left_atom, right_atoms)
				first_seq = "{[" + first_left + "]" + " seq " + "[" + first_right + "]}"
				second_seq = "{[" + second_left + "]" + " seq " + "[" + second_right + "]}"
				new_seq = first_seq + " and " + second_seq
				return new_seq
	if (rule == "Rule P6b"):
		for atom in left_atoms:
			left_atom = ""
			right_atom = ""
			# Single Atom and Single Atom
			if (re.match("^\w+ iff \w+$", atom)):
				side = re.match("^(\w+) iff (\w+)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
			# Single Atom and ()
			elif (re.match("^\w+ iff \(.+\)$", atom)):
				side = re.match("^(\w+) iff \((.*)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				right_atom = negCheck(right_atom)
			# () and Single Atom
			elif (re.match("^\(.+\) iff \w+$", atom)):
				side = re.match("^\((.*)\) iff (\w+)$", atom)
				left_atom = side.group(1)
				left_atom = negCheck(left_atom)
				right_atom = side.group(2)
			# () and ()
			elif (re.match("^\(.+\) iff \(.+\)$", atom)):
				side = re.match("^\((.+)\) iff \((.+)\)$", atom)
				left_atom = side.group(1)
				right_atom = side.group(2)
				left_atom = negCheck(left_atom)
				right_atom = negCheck(right_atom)
			# Apply
			if (left_atom != "" and right_atom != ""):
				left_atoms.remove(atom)
				left_atoms.append(left_atom)
				first_left = insertTmpAtom(right_atom, left_atoms)
				left_atoms.remove(left_atom)
				second_left = process(left_atoms)
				right_atoms.append(right_atom)
				second_right = insertTmpAtom(left_atom, right_atoms)
				right_atoms.remove(right_atom)
				first_seq = "{[" + first_left + "]" + " seq " + "[" + right_side + "]}"
				second_seq = "{[" + second_left + "]" + " seq " + "[" + second_right + "]}"
				new_seq = first_seq + " and " + second_seq
				return new_seq
	if (rule == "NO RULE"):
		return "NO RULE"
	# Process
	left_side = process(left_atoms)
	right_side = process(right_atoms)
	new_sequent = "[" + left_side + "]" + " seq " + "[" + right_side + "]"
	return new_sequent

# Function to return list for appending list
# without changing it
def insertTmpAtom(atom, atom_list):
	atom_list.append(atom)
	tmpList = process(atom_list)
	atom_list.remove(atom)
	return tmpList

# Function to put brackets around the negation
# As the program only handles a standard format
def negCheck(atom):
	new_atom = atom
	if (re.match('^neg \w+$', atom)):
		new_atom = "(" + atom + ")"
	return new_atom

# Function to add the proof to print out
def proofAdd(sequent, rule):
	proofString = sequent + " [" + rule + "]"
	proofs.append(proofString)

# Function to split sequent to two sides
def sequentSplit(sequent):
	side = re.match(r'^\[(.*)\]\s*seq\s*\[(.*)\]$', sequent)
	if (side.group(1)):
		left_side = side.group(1)
	else:
		left_side = ""
	right_side = side.group(2)
	return (left_side, right_side)

# Function to process each atomic formula
# into a string format
def process(atoms):
	for atom in atoms:
		if (atom == ""):
			atoms.remove(atom)
	side = ""
	for atom in atoms:
		if (side == ""):
			side = atom
		else:
			side = side + ", " + atom
	return side

# Function to return string containing the rule
# That is applied in backwards direction
def applyRule(left_side, right_side):
	left_atoms = re.split(r', ', left_side)
	right_atoms = re.split(r', ', right_side)
	# Rule P1
	for atom in left_atoms:
		if atom in right_atoms: # Rule P1 - one atom matches other
			return "Rule P1" 
	# Rule P6b
	for atom in left_atoms:
		if (re.match(".* iff .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ iff \w+$", atom)):
				return "Rule P6b"
			# Single Atom and ()
			if (re.match("^\w+ iff \(.*\)$", atom)):
				return "Rule P6b"
			# () and Single Atom
			if (re.match("^\(.*\) iff \w+$", atom)):
				return "Rule P6b"
			# () and ()
			if (re.match("^\(.*\) iff \(.*\)$", atom)):
				return "Rule P6b"
	# Rule P6a
	for atom in right_atoms:
		if (re.match(".* iff .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ iff \w+$", atom)):
				return "Rule P6a"
			# Single Atom and ()
			if (re.match("^\w+ iff \(.*\)$", atom)):
				return "Rule P6a"
			# () and Single Atom
			if (re.match("^\(.*\) iff \w+$", atom)):
				return "Rule P6a"
			# () and ()
			if (re.match("^\(.*\) iff \(.*\)$", atom)):
				return "Rule P6a"
	# Rule P5b
	for atom in left_atoms:
		if (re.match(".* imp .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ imp \w+$", atom)):
				return "Rule P5b"
			# Single Atom and ()
			if (re.match("^\w+ imp \(.*\)$", atom)):
				return "Rule P5b"
			# () and Single Atom
			if (re.match("^\(.*\) imp \w+$", atom)):
				return "Rule P5b"
			# () and ()
			if (re.match("^\(.*\) imp \(.*\)$", atom)):
				return "Rule P5b"
	# Rule P5a
	for atom in right_atoms:
		if (re.match(".* imp .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ imp \w+$", atom)):
				return "Rule P5a"
			# Single Atom and ()
			if (re.match("^\w+ imp \(.*\)$", atom)):
				return "Rule P5a"
			# () and Single Atom
			if (re.match("^\(.*\) imp \w+$", atom)):
				return "Rule P5a"
			# () and ()
			if (re.match("^\(.*\) imp \(.*\)$", atom)):
				return "Rule P5a"
	# Rule P4b
	for atom in left_atoms:
		if (re.match(".* or .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ or \w+$", atom)):
				return "Rule P4b"
			# Single Atom and ()
			if (re.match("^\w+ or \(.*\)$", atom)):
				return "Rule P4b"
			# () and Single Atom
			if (re.match("^\(.*\) or \w+$", atom)):
				return "Rule P4b"
			# () and ()
			if (re.match("^\(.*\) or \(.*\)$", atom)):
				return "Rule P4b"
	# Rule P4a
	for atom in right_atoms:
		if (re.match(".* or .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ or \w+$", atom)):
				return "Rule P4a"
			# Single Atom and ()
			if (re.match("^\w+ or \(.*\)$", atom)):
				return "Rule P4a"
			# () and Single Atom
			if (re.match("^\(.*\) or \w+$", atom)):
				return "Rule P4a"
			# () and ()
			if (re.match("^\(.*\) or \(.*\)$", atom)):
				return "Rule P4a"
	# Rule P3b
	for atom in left_atoms:
		if (re.match(".* and .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ and \w+$", atom)):
				return "Rule P3b"
			# Single Atom and ()
			if (re.match("^\w+ and \(.*\)$", atom)):
				return "Rule P3b"
			# () and Single Atom
			if (re.match("^\(.*\) and \w+$", atom)):
				return "Rule P3b"
			# () and ()
			if (re.match("^\(.*\) and \(.*\)$", atom)):
				return "Rule P3b"
	# Rule P3a
	for atom in right_atoms:
		if (re.match(".* and .*", atom)):
			# Single Atom and Single Atom
			if (re.match("^\w+ and \w+$", atom)):
				return "Rule P3a"
			# Single Atom and ()
			if (re.match("^\w+ and \(.*\)$", atom)):
				return "Rule P3a"
			# () and Single Atom
			if (re.match("^\(.*\) and \w+$", atom)):
				return "Rule P3a"
			# () and ()
			if (re.match("^\(.*\) and \(.*\)$", atom)):
				return "Rule P3a"
	# Rule P2b
	for atom in left_atoms:
		if (re.match("^neg\(.*\)$", atom) or re.match("\(neg \w+\)", atom)):
			return "Rule P2b"
	# Rule P2a
	for atom in right_atoms:
		if (re.match("^neg\(.*\)$", atom) or re.match("\(neg \w+\)", atom)):
			return "Rule P2a"
	# If all falls through, then there is no rule to be applied
	return "NO RULE"

main()